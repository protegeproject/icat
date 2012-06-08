package edu.stanford.bmir.protege.web.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import edu.stanford.bmir.protege.web.client.rpc.NotificationInterval;
import edu.stanford.bmir.protege.web.client.rpc.NotificationType;
import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.util.interval.TimeIntervalCalculator;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.Log;

/**
 * Server-side services for notification management.
 */
public class NotificationSchedulerServlet extends HttpServlet {

    private static final long serialVersionUID = -7869180204904741210L;

    private Timer timer;
    Logger logger = Log.getLogger(getClass());

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        timer = new Timer();
        if (ApplicationProperties.getImmediateThreadsEnabled()) {
            timer.schedule(new NotificationScheduler(NotificationInterval.IMMEDIATELY),
                    2 * 1000 * 60,     // delay
                    2 * 1000 * 60);    //period
        }
        timer.schedule(new NotificationScheduler(NotificationInterval.DAILY),
                5 * 1000 * 60,     // delay
                NotificationInterval.DAILY.getIntervalInMilliseconds());    //period
        timer.schedule(new NotificationScheduler(NotificationInterval.HOURLY),
                10 * 1000 * 60,     // delay
                NotificationInterval.HOURLY.getIntervalInMilliseconds());    //period
    }

    @Override
    public void destroy() {
        timer.cancel();
    }

    private class NotificationScheduler extends TimerTask {
        private final NotificationInterval interval;

        private NotificationScheduler(NotificationInterval interval) {
            this.interval = interval;
        }

        /**
         * The run method.
         * <p/>
         * Note that this method will only run when scheduled; there is nothing to do with scheduling in this method.
         */
        @Override
        public void run() {
            if (!ApplicationProperties.getAllNotificationEnabled()) {
                return ;
            }

            final Date now = new Date();
            Map<String, List<ChangeDataWithProject>> changeData = new HashMap<String, List<ChangeDataWithProject>>();

            final Collection<ServerProject<Project>> projects = Protege3ProjectManager.getProjectManager().getOpenServerProjects();

            Date lastRunTime = null;
            String threadIntervalPropertyValue = null;
            final String intervalPropertyName = interval.getValue();
            for (ServerProject<Project> serverProject : projects) {
                if (serverProject == null) {
                    continue;
                }
                try {

                    KnowledgeBase kb = serverProject.getProject().getKnowledgeBase();
                    final KnowledgeBase changesKb = ChAOKbManager.getChAOKb(kb);

                    if (changesKb == null) {
                        logger.severe("Unable to open Chao KB of " + serverProject.getProjectName());
                        continue;
                    }
                    // first run only - initialize lastRunTime
                    if (lastRunTime == null) {
                        lastRunTime = new Date(0);
                        threadIntervalPropertyValue = System.getProperty(intervalPropertyName);
                        if (threadIntervalPropertyValue != null) {
                            lastRunTime = new Date(new Long(threadIntervalPropertyValue));
                        }
                    }
                    getOntologyChanges(serverProject.getProjectName(), lastRunTime, now, changeData, kb, changesKb);
                    getAnnotationChanges(serverProject.getProjectName(), lastRunTime, now, changeData, changesKb);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Caught error in email notification thread, continuing.", e);
                }
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Notifying " + changeData);
            }
            for (Map.Entry<String, List<ChangeDataWithProject>> userToChanges : changeData.entrySet()) {
                List<ChangeDataWithProject> changes = userToChanges.getValue();
                if (changes.isEmpty()) {
                    continue;
                }
                final User user = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject().getUser(userToChanges.getKey());
                final NotificationInterval frequencyOntology = NotificationInterval.fromString(user.getPropertyValue(NotificationType.ONTOLOGY.getValue()));
                if (frequencyOntology == null) {
                    continue;
                }
                changes = removeUnwantedChanges(changes, frequencyOntology, NotificationType.ONTOLOGY);
                final NotificationInterval frequencyComment = NotificationInterval.fromString(user.getPropertyValue(NotificationType.COMMENT.getValue()));
                if (frequencyComment == null) {
                    continue;
                }
                changes = removeUnwantedChanges(changes, frequencyComment, NotificationType.COMMENT);
                Collections.sort(changes, new Comparator<ChangeDataWithProject>() {
                    public int compare(ChangeDataWithProject o1, ChangeDataWithProject o2) {
                        return new Long(o1.getTimestamp().getTime() - o2.getTimestamp().getTime()).intValue();
                    }
                });
                if (!changes.isEmpty()) {
                    sendNotification(userToChanges.getKey(), changes);
                }
            }


            if (this.interval.equals(NotificationInterval.DAILY)) {
                purgeCache(now);
            }

            System.setProperty(intervalPropertyName, new Long(now.getTime()).toString());
        }

        private List<ChangeDataWithProject> removeUnwantedChanges(final List<ChangeDataWithProject> changes, final NotificationInterval frequencyOntology, final NotificationType notificationType) {
            List<ChangeDataWithProject> tempChanges = new ArrayList<ChangeDataWithProject>(changes);
            if (!frequencyOntology.equals(this.interval)) {
                for (ChangeDataWithProject change : changes) {
                    if (change.getType().equals(notificationType)) {
                        tempChanges.remove(change);
                    }
                }
            }
            return tempChanges;
        }

        /**
         * clean up cache by making sure that we use only the earliest time of the immediate/hourly/daily runs, preventing the last thread (daily) from purging elements the other threads have yet to use
         *
         * @param now
         */
        private void purgeCache(Date now) {
            Long earliestCommentTime = new Long(now.getTime());
            String hourlyProperty = NotificationInterval.HOURLY.getValue();
            String immediatelyProperty = NotificationInterval.IMMEDIATELY.getValue();
            String hourlyValue = System.getProperty(hourlyProperty);
            String immediatelyValue = System.getProperty(immediatelyProperty);

            if (hourlyValue != null) {
                if (earliestCommentTime > Long.parseLong(hourlyValue)) {
                    earliestCommentTime = Long.parseLong(hourlyValue);
                }
            }
            if (immediatelyValue != null) {
                if (earliestCommentTime > Long.parseLong(immediatelyValue)) {
                    earliestCommentTime = Long.parseLong(immediatelyValue);
                }
            }
            AnnotationCache.purge(new Date(0), new Date(earliestCommentTime));
        }

        /**
         * Retrieves the notifications, and adds the changes to a map.
         * <p/>
         * Note that the map is userName -> List of change data. This allows us to easily post-process, as we know the changes that each user
         * needs to be informed of.
         *
         * @param projectName
         * @param lastRunDate
         * @param end
         * @param collector
         * @param knowledgeBase
         * @param changesKb
         */
        private void getOntologyChanges(String projectName, final Date lastRunDate, Date end, Map<String, List<ChangeDataWithProject>> collector, KnowledgeBase knowledgeBase, final KnowledgeBase changesKb) {
            //first get a list of all watched branches, and map them to the users ....
            OntologyComponentFactory factory = new OntologyComponentFactory(changesKb);
            final Map<String, List<String>> watchedBranchNodeToUserMap = new HashMap<String, List<String>>();
            final Collection<edu.stanford.bmir.protegex.chao.ontologycomp.api.User> allUserObjects = factory.getAllUserObjects();
            for (edu.stanford.bmir.protegex.chao.ontologycomp.api.User allUserObject : allUserObjects) {
                if (allUserObject.getWatchedBranch() == null || allUserObject.getWatchedBranch().isEmpty()) {
                    continue;
                }
                final Collection<Ontology_Component> branches = allUserObject.getWatchedBranch();
                for (Ontology_Component branch : branches) {
                    List<String> users = watchedBranchNodeToUserMap.get(branch);
                    if (users == null) {
                        users = new ArrayList<String>();
                    }
                    final Collection<edu.stanford.bmir.protegex.chao.ontologycomp.api.User> by = branch.getWatchedBranchBy();
                    for (edu.stanford.bmir.protegex.chao.ontologycomp.api.User user : by) {
                        users.add(user.getName());
                    }
                    watchedBranchNodeToUserMap.put(branch.getCurrentName(), users);
                }
            }

            // now do the actual 'work' of calculating changes during our time interval ....
            TimeIntervalCalculator timeIntervalCalculator = TimeIntervalCalculator.get(changesKb);
            Collection<Change> changes = timeIntervalCalculator.getTopLevelChanges(lastRunDate, end);

            for (Change change : changes) {
                // first for the 'vanilla' or entity watches, where the user is interested only in the node itself ...
                Collection<edu.stanford.bmir.protegex.chao.ontologycomp.api.User> users = change.getApplyTo().getWatchedBy();
                for (edu.stanford.bmir.protegex.chao.ontologycomp.api.User user : users) {
                    List<ChangeDataWithProject> changeData = collector.get(user.getName());
                    if (changeData == null) {
                        changeData = new ArrayList<ChangeDataWithProject>();
                    }
                    changeData.add(new ChangeDataWithProject(change.getAuthor(), change.getContext(), change.getTimestamp().getDateParsed(), projectName, change.getApplyTo().getComponentType(), change.getApplyTo().getInitialName(), NotificationType.ONTOLOGY));
                    collector.put(user.getName(), changeData);
                }

                // if the name is null, then we're a delete, which we don't handle.
                final String s = change.getApplyTo().getCurrentName();
                if (s != null) {
                    final Cls cls = knowledgeBase.getCls(s);
                    if (cls != null) {

                        final Collection superclasses = cls.getSuperclasses();

                        for (Iterator iterator = superclasses.iterator(); iterator.hasNext();) {
                            Cls superclass = (Cls) iterator.next();
                            final String superclassName = superclass.getFrameID().getName();
                            if (watchedBranchNodeToUserMap.containsKey(superclassName)) {
                                final List<String> userNames = watchedBranchNodeToUserMap.get(superclassName);
                                for (String userName : userNames) {
                                    List<ChangeDataWithProject> changeData = collector.get(userName);
                                    if (changeData == null) {
                                        changeData = new ArrayList<ChangeDataWithProject>();
                                    }
                                    changeData.add(new ChangeDataWithProject(change.getAuthor(), change.getContext(), change.getTimestamp().getDateParsed(), projectName, change.getApplyTo().getComponentType(), change.getApplyTo().getInitialName(), NotificationType.ONTOLOGY));
                                    collector.put(userName, changeData);
                                }
                            }

                        }
                    }
                }
            }

        }
    }

    private void getAnnotationChanges(String projectName, final Date lastRunDate, Date end, Map<String, List<ChangeDataWithProject>> map, final KnowledgeBase changesKb) {
        final Slot slot = changesKb.getSlot("author");
        final Collection<Instance> instanceCollection = AnnotationCache.getAnnotations(changesKb.getProject().getName(), lastRunDate, end);
        for (Instance instance : instanceCollection) {
            final Ontology_Component rootNode = AnnotationCache.getRootNode(instance);

            final Collection<edu.stanford.bmir.protegex.chao.ontologycomp.api.User> users = rootNode.getWatchedBy();
            for (edu.stanford.bmir.protegex.chao.ontologycomp.api.User user : users) {
                List<ChangeDataWithProject> changeData = map.get(user.getName());
                if (changeData == null) {
                    changeData = new ArrayList<ChangeDataWithProject>();
                }
                final Object value = instance.getDirectOwnSlotValue(slot);
                changeData.add(new ChangeDataWithProject(value.toString(), instance.getBrowserText(), AnnotationCache.getChangeDate(changesKb.getProject().getName(), instance), projectName, rootNode.getComponentType(), rootNode.getInitialName(), NotificationType.COMMENT));
                map.put(user.getName(), changeData);
            }
        }

    }

    private void sendNotification(final String userName, final Collection<ChangeDataWithProject> changes) {

        final String userEmail = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getUserEmail(userName);
        StringBuffer stringBuffer = new StringBuffer();
        MessageFormat format = new MessageFormat("{1,date} {1,time}: {2} made the change: {0}\n");
        MessageFormat linkFormat = new MessageFormat("\tjump to change: http://{0}:{1}/WebProtege.html?ontology={2}&tab={3}&id={4}\n");
        for (ChangeDataWithProject change : changes) {

            format.format(new Object[]{change.getDescription(), change.getTimestamp(), change.getAuthor()}, stringBuffer, new FieldPosition(0));

            try {
                linkFormat.format(new Object[]{
                        ApplicationProperties.getApplicationUrl(),
                        ApplicationProperties.getApplicationPort(),
                        URLEncoder.encode(change.getProject(), "UTF-8"),
                        "Individual".equals(change.getValueType()) ? URLEncoder.encode("IndividualsTab", "UTF-8") : URLEncoder.encode("ClassesTab", "UTF-8"),
                        URLEncoder.encode(change.getName(), "UTF-8")}, stringBuffer, new FieldPosition(0));
            } catch (UnsupportedEncodingException e) {
                Log.getLogger().log(Level.SEVERE, "Error formatting to URLEncoding projectName = " + change.getProject() + ", tab = " + change.getValueType() + ", id = " + change.getName(), e);
            }
        }
        final String subject = MessageFormat.format("WebProtege change report generated on {0,date} {0,time}", new Date());
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "emailing user '" + userEmail + "' with subject '" + subject + "' message '" + stringBuffer.toString() + "'");
        }
        EmailUtil.sendEmail(userEmail, subject, stringBuffer.toString(), ApplicationProperties.getEmailAccount());
    }


    class ChangeDataWithProject {
        private final String author;
        private final String description;
        private final Date timestamp;
        private final String project;
        private final String valueType;
        private final String name;
        private final NotificationType type;

        public ChangeDataWithProject(String author, String description, Date timestamp, String project, String valueType, String name, NotificationType type) {
            this.author = author;
            this.description = description;
            this.timestamp = timestamp;
            this.project = project;
            this.valueType = valueType;
            this.name = name;
            this.type = type;
        }

        public NotificationType getType() {
            return type;
        }

        public String getProject() {
            return project;
        }

        public String getAuthor() {
            return author;
        }

        public String getDescription() {
            return description;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public String getValueType() {
            return valueType;
        }

        public String getName() {
            return name;
        }
    }
}
