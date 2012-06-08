package org.semanticweb.owlapi.util;

import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.Namespaces;

import java.util.*;
/*
 * Copyright (C) 2008, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 10-Sep-2008<br><br>
 */
public class DefaultPrefixManager implements PrefixManager, ShortFormProvider, IRIShortFormProvider {

    private Map<String, String> prefix2NamespaceMap;


    /**
     * Creates a namespace manager that does not have a default namespace.
     */
    public DefaultPrefixManager() {
        prefix2NamespaceMap = new HashMap<String, String>();
        setupDefaultPrefixes();
    }

    public DefaultPrefixManager(PrefixManager pm) {
        prefix2NamespaceMap = new HashMap<String, String>();
        for(String prefixName : pm.getPrefixNames()) {
            prefix2NamespaceMap.put(prefixName, pm.getPrefix(prefixName));
        }
        setupDefaultPrefixes();
    }

    public void clear() {
        // Clear the default namespace and map
        prefix2NamespaceMap.clear();
    }

    /**
     * Gets the prefix names that have a mapping in this prefix manager
     */
    public Set<String> getPrefixNames() {
        return new HashSet<String>(prefix2NamespaceMap.keySet());
    }

    /**
     * Creates a namespace manager that has the specified default namespace.
     * @param defaultPrefix The namespace to be used as the default namespace.
     */
    public DefaultPrefixManager(String defaultPrefix) {
        prefix2NamespaceMap = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                int diff = o1.length() - o2.length();
                if(diff != 0) {
                    return diff;
                }
                else {
                    return o1.compareTo(o2);
                }
            }
        });

        if (defaultPrefix != null) {
            setDefaultPrefix(defaultPrefix);
        }
        setupDefaultPrefixes();
    }

    private void setupDefaultPrefixes() {
        setPrefix("owl:", Namespaces.OWL.toString());
        setPrefix("rdfs:", Namespaces.RDFS.toString());
        setPrefix("rdf:", Namespaces.RDF.toString());
        setPrefix("xsd:", Namespaces.XSD.toString());
        setPrefix("skos:", Namespaces.SKOS.toString());
        setPrefix("xml:", Namespaces.XML.toString());
    }


    /**
     * Sets the default namespace.  This will also bind the prefix name ":" to this prefix
     * @param defaultPrefix The namespace to be used as the default namespace.  Note that
     * the value may be <code>null</code> in order to clear the default namespace.
     */
    public void setDefaultPrefix(String defaultPrefix) {
        setPrefix(":", defaultPrefix);
    }

    public String getPrefixIRI(IRI iri) {
        String iriString = iri.toString();
        for(String prefixName : prefix2NamespaceMap.keySet()) {
            String prefix = prefix2NamespaceMap.get(prefixName);
            if(iriString.startsWith(prefix)) {
                StringBuilder sb = new StringBuilder();
                sb.append(prefixName);
                String localName = iriString.substring(prefix.length());
                sb.append(localName);
                return sb.toString();
            }
        }
        return null;
    }

    public String getDefaultPrefix() {
        return prefix2NamespaceMap.get(":");
    }


    public boolean containsPrefixMapping(String prefix) {
        return prefix2NamespaceMap.containsKey(prefix) &&
                prefix2NamespaceMap.get(prefix) != null;
    }


    public IRI getIRI(String curie) {
        if(curie.startsWith("<")) {
            return IRI.create(curie.substring(1, curie.length() - 1));
        }
        int sep = curie.indexOf(':');
        if(sep == -1) {
            if (getDefaultPrefix() != null) {
                return IRI.create(getDefaultPrefix() + curie);
            }
            else {
                return IRI.create(curie);
            }
        }
        else {
            String prefixName = curie.substring(0, sep + 1);
            if(!containsPrefixMapping(prefixName)) {
                throw new RuntimeException("Prefix not registered for prefix name: " + prefixName);
            }
            String prefix = getPrefix(prefixName);
            String localName = curie.substring(sep + 1);
            return IRI.create(prefix + localName);
        }
    }

    public Map<String, String> getPrefixName2PrefixMap() {
        return Collections.unmodifiableMap(prefix2NamespaceMap);
    }

    public String getPrefix(String prefixName) {
        return prefix2NamespaceMap.get(prefixName);
    }


    /**
     * Adds a prefix name to prefix mapping
     * @param prefixName name The prefix name (must not be null)
     * @param prefix The prefix
     * @throws NullPointerException if the prefix name or prefix is <code>null</code>.
     * @throws IllegalArgumentException if the prefix name does not end with a colon.
     */
    public void setPrefix(String prefixName, String prefix) {
        if(prefix == null) {
            throw new NullPointerException("Prefix name must not be null");
        }
        if(!prefixName.endsWith(":")) {
            throw new IllegalArgumentException("Prefix names must end with a colon (:)");
        }
        prefix2NamespaceMap.put(prefixName, prefix);
    }


    /**
     * Removes a previously registerd prefix namespace mapping
     * @param namespace The namespace to be removed.
     */
    public void unregisterNamespace(String namespace) {
        for(Iterator<String> it = prefix2NamespaceMap.values().iterator(); it.hasNext(); ) {
            if(it.next().equals(namespace)) {
                it.remove();
                return;
            }
        }
    }

    public String getShortForm(IRI iri) {
        String sf = getPrefixIRI(iri);
        if(sf == null) {
            return iri.toQuotedString();
        }
        else {
            return sf;
        }
    }

    public String getShortForm(OWLEntity entity) {
        return getShortForm(entity.getIRI());
    }



    public void dispose() {
    }
}
