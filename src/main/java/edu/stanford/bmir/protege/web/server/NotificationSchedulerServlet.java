package edu.stanford.bmir.protege.web.server;

import edu.stanford.bmir.protege.web.client.rpc.NotificationInterval;
import edu.stanford.bmir.protege.web.client.rpc.NotificationType;
import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.Transaction;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.server_changes.RetrieveChangesProtegeJob;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server-side services for notification management.
 */
public class NotificationSchedulerServlet extends HttpServlet {
    private static final long serialVersionUID = -7869180204904741210L;

    private static final String PROPERTY_PREFIX = "scheduler.last.run.time.";

    Logger logger = Log.getLogger(getClass());
    private ScheduledExecutorService service;
    private Map<NotificationInterval, Boolean> hasRescheduledTaskPending = new HashMap<NotificationInterval, Boolean>();
    private final MessageFormat ontologyChangeMessage = new MessageFormat("{1,date} {1,time}: {2} made the change: \n\t{0}\n");
    private final MessageFormat noteChangeMessage = new MessageFormat("{1,date} {1,time}: {2} added a new comment: \n\t{0}\n");
    private final MessageFormat linkMessage = new MessageFormat("\tDirect link: http://{0}?ontology={1}&tab={2}&id={3}\n\n");

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory(){
            public Thread newThread(Runnable r) {
                final Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("Email Notification Thread");
                return thread;
            }
        });
        if (ApplicationProperties.getImmediateThreadsEnabled()) {
            service.scheduleAtFixedRate(new NotificationScheduler(NotificationInterval.IMMEDIATELY),
                    ApplicationProperties.getImmediateThreadStartupDelay(),     // delay
                    ApplicationProperties.getImmediateThreadInterval(), //period
                    TimeUnit.MILLISECONDS);
        }
        service.scheduleAtFixedRate(new NotificationScheduler(NotificationInterval.DAILY),
                ApplicationProperties.getDailyThreadStartupDelay(),     // delay
                NotificationInterval.DAILY.getIntervalInMilliseconds(), //period
                TimeUnit.MILLISECONDS);
        service.scheduleAtFixedRate(new NotificationScheduler(NotificationInterval.HOURLY),
                ApplicationProperties.getHourlyThreadStartupDelay(),     // delay
                NotificationInterval.HOURLY.getIntervalInMilliseconds(),   //period
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void destroy() {
        service.shutdownNow();
    }

    private class NotificationScheduler extends TimerTask {
        private final NotificationInterval interval;
        private boolean isRescheduled = false;

        private NotificationScheduler(NotificationInterval interval) {
            this.interval = interval;
        }

        private NotificationScheduler(NotificationInterval interval, boolean rescheduled) {
            this.interval = interval;
            isRescheduled = rescheduled;
        }

        /**
         * The run method.
         * <p/>
         * Note that this method will only run when scheduled; there is nothing to do with scheduling in this method.
         */
        @Override
        public void run() {
            try {
            if (!ApplicationProperties.getAllNotificationEnabled()) {
                return;
            }
            if (hasRescheduledTaskPending.get(interval) != null && hasRescheduledTaskPending.get(interval) && !isRescheduled){
                return;
            }

            final Date now = new Date();
            if (logger.isLoggable(Level.FINE)){
                logger.fine("running interval=" + interval + " isRescheduled="+isRescheduled);
            }
            Map<String, Set<ChangeDataWithProject>> changeData = new HashMap<String, Set<ChangeDataWithProject>>();

            final Collection<ServerProject<Project>> projects = Protege3ProjectManager.getProjectManager().getOpenServerProjects();
            final String intervalPropertyName = PROPERTY_PREFIX + interval.getValue();
            final MetaProject metaProject = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject();
            final Set<ServerProject<Project>> notReadySet = new HashSet<ServerProject<Project>>();
            for (ServerProject<Project> serverProject : projects) {
                if (serverProject == null || serverProject.getProject() == null || !serverProject.isLoaded()) {
                    notReadySet.add(serverProject);
                    continue;
                }
                try {
                    Date lastRunTime = null;
                    final ProjectInstance instance = metaProject.getProject(serverProject.getProjectName());
                    final String threadIntervalPropertyValue = instance.getPropertyValue(intervalPropertyName);
                    if (threadIntervalPropertyValue != null) {
                        lastRunTime = new Date(Long.parseLong(threadIntervalPropertyValue));
                    }

                    KnowledgeBase kb = serverProject.getProject().getKnowledgeBase();
                    final KnowledgeBase changesKb = ChAOKbManager.getChAOKb(kb);

                    if (changesKb == null) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "Unable to open Chao KB of " + serverProject.getProjectName());
                        }
                        continue;
                    }
                    // first run only - initialize lastRunTime
                    if (lastRunTime == null) {
                        // if we do not have a last run date, then the last run date was 24 hours ago
                        lastRunTime = new Date(new Date().getTime() - (1000 * 60 * 24));

                    }
                    final boolean shouldContinue = getOntologyChanges(serverProject.getProjectName(), lastRunTime, now, changeData, kb, changesKb);
                    if (!shouldContinue){
                        hasRescheduledTaskPending.put(interval,Boolean.TRUE);
                        return;
                    }
                    getAnnotationChanges(serverProject.getProjectName(), lastRunTime, now, changeData, changesKb);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Caught error in email notification thread, continuing.", e);
                }
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Notifying " + changeData);
            }
            for (Map.Entry<String, Set<ChangeDataWithProject>> userToChanges : changeData.entrySet()) {
                Set<ChangeDataWithProject> changes = userToChanges.getValue();
                if (changes.isEmpty()) {
                    continue;
                }
                final User user = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject().getUser(userToChanges.getKey());
                if (user == null) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "Could not find user " + userToChanges.getKey() + " in MetaProject, continuing.");
                    }
                    continue;
                }
                final NotificationInterval frequencyOntology = NotificationInterval.fromString(user.getPropertyValue(NotificationType.ONTOLOGY.getValue()));
                NotificationInterval frequencyComment = NotificationInterval.fromString(user.getPropertyValue(NotificationType.COMMENT.getValue()));
                //if user has not set their frequency, then we default to immediate notification.
                if (frequencyOntology == null) {
                    changes = removeUnwantedChanges(changes, NotificationInterval.IMMEDIATELY, NotificationType.ONTOLOGY);
                } else {
                    changes = removeUnwantedChanges(changes, frequencyOntology, NotificationType.ONTOLOGY);
                }
                //if user has not set their frequency, then we default to immediate notification.
                if (frequencyComment == null) {
                    changes = removeUnwantedChanges(changes, NotificationInterval.IMMEDIATELY, NotificationType.COMMENT);
                } else {
                    changes = removeUnwantedChanges(changes, frequencyComment, NotificationType.COMMENT);
                }
                List<ChangeDataWithProject> sortedChanges = new ArrayList<ChangeDataWithProject>(changes);
                Collections.sort(sortedChanges, new Comparator<ChangeDataWithProject>() {
                    public int compare(ChangeDataWithProject o1, ChangeDataWithProject o2) {
                        return new Long(o1.getTimestamp().getTime() - o2.getTimestamp().getTime()).intValue();
                    }
                });
                if (!changes.isEmpty()) {
                    sendNotification(userToChanges.getKey(), sortedChanges);
                }
            }


            if (this.interval.equals(NotificationInterval.DAILY)) {
                purgeCache(now);
            }
            for (ServerProject<Project> serverProject : projects) {
                if (notReadySet.contains(serverProject)){
                    continue;
                }
                final ProjectInstance instance = metaProject.getProject(serverProject.getProjectName());
                String threadIntervalPropertyValue = instance.getPropertyValue(intervalPropertyName);
                while (threadIntervalPropertyValue != null) {
                    instance.removePropertyValue(intervalPropertyName, threadIntervalPropertyValue);
                    threadIntervalPropertyValue = instance.getPropertyValue(intervalPropertyName);
                }
                instance.addPropertyValue(intervalPropertyName, Long.toString(now.getTime()));
            }
            } catch(Exception e){
                logger.log(Level.SEVERE, "Caught in notification thread", e);
            }
            hasRescheduledTaskPending.put(interval, Boolean.FALSE);
        }

        private Set<ChangeDataWithProject> removeUnwantedChanges(final Set<ChangeDataWithProject> changes, final NotificationInterval frequencyOntology, final NotificationType notificationType) {
            Set<ChangeDataWithProject> tempChanges = new HashSet<ChangeDataWithProject>(changes);
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
        @SuppressWarnings("unchecked")
        private boolean getOntologyChanges(String projectName, final Date lastRunDate, Date end, Map<String, Set<ChangeDataWithProject>> collector, KnowledgeBase knowledgeBase, final KnowledgeBase changesKb) {
            //first get a list of all watched branches, and map them to the users ....
            //            OntologyComponentFactory factory = new OntologyComponentFactory(changesKb);
            //            final Map<String, List<String>> watchedBranchNodeToUserMap = new HashMap<String, List<String>>();
            //            final Collection<edu.stanford.bmir.protegex.chao.ontologycomp.api.User> allUserObjects = factory.getAllUserObjects();
            final Map<String, List<edu.stanford.bmir.protegex.chao.ontologycomp.api.User>> watchedBranchNodeToUserMap = WatchedEntitiesCache.getWatchedBranches();

            final Collection<Change> changes = (Collection<Change>) new RetrieveChangesProtegeJob(knowledgeBase,  lastRunDate, end).execute();

            // if we are in a transaction, then reschedule
            if (changes == null){
                service.schedule(new NotificationScheduler(this.interval, true), ApplicationProperties.getEmailRetryDelay(), TimeUnit.MILLISECONDS);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Found transaction in service, rescheduling " + interval + "  thread to run in " + (ApplicationProperties.getEmailRetryDelay()/1000) + " seconds.");
                }
                return false;
            }

            LocalizeUtils.localize(changes, changesKb);

            for (Change change : changes) {
                // first for the 'vanilla' or entity watches, where the user is interested only in the node itself ...
                Collection<edu.stanford.bmir.protegex.chao.ontologycomp.api.User> users = change.getApplyTo().getWatchedBy();
                for (edu.stanford.bmir.protegex.chao.ontologycomp.api.User user : users) {
                    Set<ChangeDataWithProject> changeData = collector.get(user.getName());
                    if (changeData == null) {
                        changeData = new HashSet<ChangeDataWithProject>();
                    }
                    final ChangeDataWithProject dataWithProject = new ChangeDataWithProject(change.getAuthor(), change.getContext(), change.getTimestamp().getDateParsed(), projectName, change.getApplyTo().getComponentType(), change.getApplyTo().getCurrentName(), NotificationType.ONTOLOGY);
                    if (!changeData.contains(dataWithProject)) {
                        changeData.add(dataWithProject);
                    }
                    collector.put(user.getName(), changeData);
                }

                /*
                 * TT: Code below might cause performance issues with the main ontology, especially with the coarse grain lock.
                 * With the readers-writers branch, should be fine.
                 */
                // if the name is null, then we're a delete, which we don't handle.
                final String currentName = change.getApplyTo().getCurrentName();
                if (currentName != null) {
                    Frame frame = knowledgeBase.getFrame(currentName);

                    if (frame != null && frame instanceof Cls) {

                        final Collection superclasses = ((Cls) frame).getSuperclasses();

                        for (Iterator iterator = superclasses.iterator(); iterator.hasNext();) {
                            Cls superclass = (Cls) iterator.next();
                            final String superclassName = superclass.getFrameID().getName();
                            if (watchedBranchNodeToUserMap.containsKey(superclassName)) {
                                final List<edu.stanford.bmir.protegex.chao.ontologycomp.api.User> userNames = watchedBranchNodeToUserMap.get(superclassName);
                                for (edu.stanford.bmir.protegex.chao.ontologycomp.api.User user : userNames) {
                                    Set<ChangeDataWithProject> changeData = collector.get(user.getName());
                                    if (changeData == null) {
                                        changeData = new HashSet<ChangeDataWithProject>();
                                    }
                                    changeData.add(new ChangeDataWithProject(change.getAuthor(), change.getContext(), change.getTimestamp().getDateParsed(), projectName, change.getApplyTo().getComponentType(), change.getApplyTo().getCurrentName(), NotificationType.ONTOLOGY));
                                    collector.put(user.getName(), changeData);
                                }
                            }
                        }
                    }
                }
            }
            return true;

        }
    }

    private void getAnnotationChanges(String projectName, final Date lastRunDate, Date end, Map<String, Set<ChangeDataWithProject>> map, final KnowledgeBase changesKb) {
        final Slot slot = changesKb.getSlot("author");
        final Collection<Instance> instanceCollection = AnnotationCache.getAnnotations(changesKb.getProject().getName(), lastRunDate, end);
        for (Instance instance : instanceCollection) {
            final Ontology_Component rootNode = AnnotationCache.getRootNode(instance);

            final Collection<edu.stanford.bmir.protegex.chao.ontologycomp.api.User> users = rootNode.getWatchedBy();
            for (edu.stanford.bmir.protegex.chao.ontologycomp.api.User user : users) {
                Set<ChangeDataWithProject> changeData = map.get(user.getName());
                if (changeData == null) {
                    changeData = new HashSet<ChangeDataWithProject>();
                }
                final Object value = instance.getDirectOwnSlotValue(slot);
                final String className = rootNode.getCurrentName() == null ? "" : rootNode.getCurrentName();
                changeData.add(new ChangeDataWithProject(value.toString(), instance.getBrowserText(), AnnotationCache.getChangeDate(changesKb.getProject().getName(), instance), projectName, rootNode.getComponentType(), className, NotificationType.COMMENT));
                map.put(user.getName(), changeData);
            }
        }

    }

    private void sendNotification(final String userName, final Collection<ChangeDataWithProject> changes) {

        final String userEmail = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getUserEmail(userName);
        StringBuffer stringBuffer = new StringBuffer();

        for (ChangeDataWithProject change : changes) {
            if (NotificationType.COMMENT.equals(change.getType())) {
                noteChangeMessage.format(new Object[]{Transaction.removeApplyTo(change.getDescription()), change.getTimestamp(), change.getAuthor()}, stringBuffer, new FieldPosition(0));
            } else {
                ontologyChangeMessage.format(new Object[]{Transaction.removeApplyTo(change.getDescription()), change.getTimestamp(), change.getAuthor()}, stringBuffer, new FieldPosition(0));
            }
            try {
                String tabName = getTabName(change);
                linkMessage.format(new Object[]{
                        ApplicationProperties.getApplicationUrl(),
                        URLEncoder.encode(change.getProject(), "UTF-8"),
                        tabName,
                        change.getName() == null ? "" : URLEncoder.encode(change.getName(), "UTF-8")
                }, stringBuffer, new FieldPosition(0));
            } catch (UnsupportedEncodingException e) {
                Log.getLogger().log(Level.SEVERE, "Error formatting to URLEncoding projectName = " + change.getProject() + ", tab = " + change.getValueType() + ", id = " + change.getName(), e);
            }
        }
        final String applicationName = ApplicationProperties.getApplicationName();
        stringBuffer.append("\n-----\n* To change the frequency of your notifications, or to stop receiving them altogether, please edit your profile by going to http://");
        stringBuffer.append(ApplicationProperties.getApplicationUrl());
        stringBuffer.append(" and clicking Options -> Edit Profile.");
        final String subject = MessageFormat.format("{1} change report generated on {0,date} {0,time}", new Date(), applicationName);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "emailing user '" + userEmail + "' with subject '" + subject + "' message '" + stringBuffer.toString() + "'");
        }
        EmailUtil.sendEmail(userEmail, subject, stringBuffer.toString(), ApplicationProperties.getEmailAccount());
    }

    private String getTabName(ChangeDataWithProject change) throws UnsupportedEncodingException {
        String tabName = URLEncoder.encode("ClassesTab", "UTF-8");
        if ("Individual".equals(change.getValueType())) {
            tabName = URLEncoder.encode("IndividualsTab", "UTF-8");
        } else if (NotificationType.COMMENT.equals(change.getType())) {
            tabName = "NotesTab";
        }
        return tabName;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ChangeDataWithProject)) {
                return false;
            }

            ChangeDataWithProject that = (ChangeDataWithProject) o;

            if (author != null ? !author.equals(that.author) : that.author != null) {
                return false;
            }
            if (description != null ? !description.equals(that.description) : that.description != null) {
                return false;
            }
            if (name != null ? !name.equals(that.name) : that.name != null) {
                return false;
            }
            if (project != null ? !project.equals(that.project) : that.project != null) {
                return false;
            }
            if (type != that.type) {
                return false;
            }
            if (valueType != null ? !valueType.equals(that.valueType) : that.valueType != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = author != null ? author.hashCode() : 0;
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (project != null ? project.hashCode() : 0);
            result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }
    }
}
