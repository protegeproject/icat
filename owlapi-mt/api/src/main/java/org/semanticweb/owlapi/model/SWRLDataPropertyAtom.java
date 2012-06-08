package org.semanticweb.owlapi.model;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Jan 15, 2007<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public interface SWRLDataPropertyAtom extends SWRLBinaryAtom<SWRLIArgument, SWRLDArgument> {

    /**
     * Gets the predicate of this atom
     * @return The atom predicate
     */
    OWLDataPropertyExpression getPredicate();
}
