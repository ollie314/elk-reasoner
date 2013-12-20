/**
 * 
 */
package org.semanticweb.elk.reasoner.saturation.conclusions;

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectIntersectionOf;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectSomeValuesFrom;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedPropertyChain;
import org.semanticweb.elk.reasoner.saturation.context.Context;

/**
 * Interface for objects responsible for creating instances of {@link Conclusion} during inferencing.
 * 
 * TODO may need to take side conditions in some form as an input.
 * 
 * TODO all examples are oversimplified, e.g., existentials don't consider role hierarchies, etc.
 * 
 * @author Pavel Klinov
 *
 * pavel.klinov@uni-ulm.de
 */
public interface ConclusionFactory {
	
	/**
	 * Inference of the form A => A or A => owl:Thing
	 * 
	 * @param ice
	 * @return
	 */
	public PositiveSubsumer createSubsumer(IndexedClassExpression ice);
	
	/**
	 * Inference of the form A => B, B => C is in O, thus A => C
	 * 
	 * @param premise B
	 * @param subsumer C
	 * @return C as a conclusion
	 */
	public PositiveSubsumer createSubsumer(Conclusion premise, IndexedClassExpression subsumer);
	
	/**
	 * Inference of the form A => R1 some B, B => R1 some C, thus A => R1 o R2 C.
	 * Used when compose processing a forward link.  
	 * 
	 * @param context B
	 * @param forwardLink B => R2 some C
	 * @param backwardLinkChain A => R1 some B
	 * @param chain R1 o R2
	 * @param source A 
	 * @return
	 */
	public BackwardLink createComposedBackwardLink(Context context, ForwardLink forwardLink, IndexedPropertyChain backwardLinkChain, IndexedPropertyChain chain, Context source);
	
	/**
	 * Inference of the form A => R1 some B, B => R1 some C, thus A => R1 o R2 C.
	 * Used when compose processing a backward link.
	 * 
	 * @param context B
	 * @param backwardLink A => R1 some B
	 * @param forwardLinkChain B => R2 some C
	 * @param chain R1 o R2
	 * @return
	 */
	public BackwardLink createComposedBackwardLink(Context context, BackwardLink backwardLink, IndexedPropertyChain forwardLinkChain, Context forwardTarget, IndexedPropertyChain chain);
	
	/**
	 * Creates the forward link of the form A -R-> B from a backward link A <-R- B when R can be composed.
	 * 
	 * @param subsumer
	 * @return
	 */
	public ForwardLink createForwardLink(BackwardLink backwardLink, Context target);
	
	/**
	 * Creates the backward link of the form A <-R- B when A => R some B has been derived
	 * 
	 * @param subsumer
	 * @return
	 */
	public BackwardLink createBackwardLink(IndexedObjectSomeValuesFrom subsumer, Context target);
	
	/**
	 * The first step of handling the inference of the form 
	 * A => R some B, B => C, thus A => R some C. 
	 * 
	 * This method creates an intermediate conclusion called {@link Propagation}.
	 * 
	 * @param premise B
	 * @param chain R
	 * @param carry R some C
	 * @return
	 */
	public Propagation createPropagation(Conclusion premise, IndexedPropertyChain chain, IndexedObjectSomeValuesFrom carry);
	
	/**
	 * The second step of handling the inference of the form 
	 * A => R some B, B => C, thus A => R some C. 
	 * 
	 * This method creates an actual {@link NegativeSubsumer} from the previously created propagation when processing the backward link.
	 * 
	 * @param bwLink A => R some B
	 * @param carry R some C
	 * @param context B, the context where the backward link is stored
	 * @return
	 */
	public NegativeSubsumer createPropagatedSubsumer(BackwardLink bwLink, IndexedObjectSomeValuesFrom carry, Context context);
	
	/**
	 * The second step of handling the inference of the form 
	 * A => R some B, B => C, thus A => R some C. 
	 * 
	 * This method creates an actual {@link NegativeSubsumer} when processing the previously created propagation.
	 * 
	 * @param propagation R some C
	 * @param linkRelation R
	 * @param linkTarget A
	 * @param context B (where the link is stored)
	 * @return
	 */
	public NegativeSubsumer createPropagatedSubsumer(Propagation propagation, IndexedPropertyChain linkRelation, Context linkTarget, Context context);
	
	/**
	 * Inference of the form A => B1, A => B2, thus A => B1 and B2
	 * 
	 * @param premise B1
	 * @param conjunct B2
	 * @param conjunction B1 and B2 
	 * @return 
	 */
	public NegativeSubsumer createdComposedConjunction(Conclusion premise, IndexedClassExpression conjunct, IndexedObjectIntersectionOf conjunction);
	
	/**
	 * Inference of the form A => B1 and B2, thus A => Bi (i = 1 or 2)
	 * 
	 * @param conjunction B1 and B2
	 * @param conjunct Bi
	 * @return
	 */
	public PositiveSubsumer createConjunct(IndexedObjectIntersectionOf conjunction, IndexedClassExpression conjunct);
	
	/**
	 * 
	 * @param existential
	 * @return
	 */
	public NegativeSubsumer createReflexiveSubsumer(IndexedObjectSomeValuesFrom existential);
}