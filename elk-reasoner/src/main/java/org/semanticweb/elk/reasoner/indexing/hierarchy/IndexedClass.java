/*
 * #%L
 * elk-reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.semanticweb.elk.reasoner.indexing.hierarchy;

import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.predefined.PredefinedElkClass;
import org.semanticweb.elk.reasoner.indexing.visitors.IndexedClassEntityVisitor;
import org.semanticweb.elk.reasoner.indexing.visitors.IndexedClassVisitor;
import org.semanticweb.elk.reasoner.saturation.SaturationStateWriter;
import org.semanticweb.elk.reasoner.saturation.conclusions.DecomposedSubsumer;
import org.semanticweb.elk.reasoner.saturation.conclusions.Subsumer;
import org.semanticweb.elk.reasoner.saturation.context.Context;
import org.semanticweb.elk.reasoner.saturation.rules.ChainableRule;
import org.semanticweb.elk.reasoner.saturation.rules.CompositionRuleVisitor;
import org.semanticweb.elk.reasoner.saturation.rules.SubsumerDecompositionVisitor;
import org.semanticweb.elk.util.collections.chains.Chain;
import org.semanticweb.elk.util.collections.chains.Matcher;
import org.semanticweb.elk.util.collections.chains.ModifiableLinkImpl;
import org.semanticweb.elk.util.collections.chains.ReferenceFactory;
import org.semanticweb.elk.util.collections.chains.SimpleTypeBasedMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents all occurrences of an {@link ElkClass} in an ontology.
 * 
 * @author Frantisek Simancik
 * @author "Yevgeny Kazakov"
 * 
 */
public class IndexedClass extends IndexedClassEntity {

	protected static final Logger LOGGER_ = LoggerFactory
			.getLogger(IndexedClass.class);

	/**
	 * The indexed ElkClass
	 */
	protected final ElkClass elkClass;

	/**
	 * This counts how many times this object occurred in the ontology. Because
	 * of declaration axioms, this number might differ from the sum of the
	 * negative and the positive occurrences counts
	 */
	protected int occurrenceNo = 0;

	/**
	 * Creates an object representing the given ElkClass.
	 */
	protected IndexedClass(ElkClass elkClass) {
		this.elkClass = elkClass;
	}

	/**
	 * @return The represented ElkClass.
	 */
	public ElkClass getElkClass() {
		return elkClass;
	}

	public <O> O accept(IndexedClassVisitor<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	public <O> O accept(IndexedClassEntityVisitor<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	protected void updateOccurrenceNumbers(final ModifiableOntologyIndex index,
			int increment, int positiveIncrement, int negativeIncrement) {

		if (occurrenceNo == 0 && increment > 0) {
			index.addClass(elkClass);
		}

		if (negativeOccurrenceNo == 0 && negativeIncrement > 0
				&& elkClass == PredefinedElkClass.OWL_THING) {
			index.addContextInitRule(new OwlThingContextInitializationRule());
		}

		occurrenceNo += increment;
		positiveOccurrenceNo += positiveIncrement;
		negativeOccurrenceNo += negativeIncrement;

		if (occurrenceNo == 0 && increment < 0) {
			index.removeClass(elkClass);
		}

		if (negativeOccurrenceNo == 0 && negativeIncrement < 0
				&& elkClass == PredefinedElkClass.OWL_THING) {
			index.removeContextInitRule(new OwlThingContextInitializationRule());
		}
	}

	@Override
	public String printOccurrenceNumbers() {
		return "[all=" + occurrenceNo + "; pos=" + positiveOccurrenceNo
				+ "; neg=" + +negativeOccurrenceNo + "]";
	}

	@Override
	public void checkOccurrenceNumbers() {
		if (LOGGER_.isTraceEnabled())
			LOGGER_.trace(toString() + " occurences: "
					+ printOccurrenceNumbers());
		if (occurrenceNo < 0 || positiveOccurrenceNo < 0
				|| negativeOccurrenceNo < 0)
			throw new ElkUnexpectedIndexingException(toString()
					+ " has a negative occurrence: " + printOccurrenceNumbers());
	}

	@Override
	public boolean occurs() {
		return occurrenceNo > 0;
	}

	@Override
	public void accept(SubsumerDecompositionVisitor visitor, Context context) {
		visitor.visit(this, context);
	}

	@Override
	public String toStringStructural() {
		return '<' + getElkClass().getIri().getFullIriAsString() + '>';
	}

	/**
	 * A context initialization rule that produces {@link Subsumer}
	 * {@code owl:Thing} in a context. It should be applied only if
	 * {@code owl:Thing} occurs negatively in the ontology.
	 */
	public static class OwlThingContextInitializationRule extends
			ModifiableLinkImpl<ChainableRule<Void>> implements
			ChainableRule<Void> {

		private static final String NAME_ = "owl:Thing Introduction";

		private OwlThingContextInitializationRule(ChainableRule<Void> tail) {
			super(tail);
		}

		public OwlThingContextInitializationRule() {
			super(null);
		}

		@Override
		public String getName() {
			return NAME_;
		}

		@Override
		public void apply(Void premise, Context context,
				SaturationStateWriter writer) {
			LOGGER_.trace("Applying {} to {}", NAME_, context);

			writer.produce(context,
					new DecomposedSubsumer(writer.getOwlThing()));
		}

		private static final Matcher<ChainableRule<Void>, OwlThingContextInitializationRule> MATCHER_ = new SimpleTypeBasedMatcher<ChainableRule<Void>, OwlThingContextInitializationRule>(
				OwlThingContextInitializationRule.class);

		private static final ReferenceFactory<ChainableRule<Void>, OwlThingContextInitializationRule> FACTORY_ = new ReferenceFactory<ChainableRule<Void>, OwlThingContextInitializationRule>() {
			@Override
			public OwlThingContextInitializationRule create(
					ChainableRule<Void> tail) {
				return new OwlThingContextInitializationRule(tail);
			}
		};

		@Override
		public boolean addTo(Chain<ChainableRule<Void>> ruleChain) {
			OwlThingContextInitializationRule rule = ruleChain.find(MATCHER_);

			if (rule == null) {
				ruleChain.getCreate(MATCHER_, FACTORY_);
				return true;
			}
			return false;
		}

		@Override
		public boolean removeFrom(Chain<ChainableRule<Void>> ruleChain) {
			return ruleChain.remove(MATCHER_) != null;
		}

		@Override
		public void accept(CompositionRuleVisitor visitor, Void premise,
				Context context, SaturationStateWriter writer) {
			visitor.visit(this, context, writer);
		}

	}
}
