package org.semanticweb.elk.reasoner.indexing.conversion;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.interfaces.ElkClassAssertionAxiom;
import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.owl.interfaces.ElkDeclarationAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDifferentIndividualsAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDisjointClassesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDisjointUnionAxiom;
import org.semanticweb.elk.owl.interfaces.ElkEquivalentClassesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkEquivalentObjectPropertiesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkIndividual;
import org.semanticweb.elk.owl.interfaces.ElkObject;
import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyAssertionAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyChain;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyDomainAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyExpression;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyRangeAxiom;
import org.semanticweb.elk.owl.interfaces.ElkReflexiveObjectPropertyAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSameIndividualAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSubClassOfAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSubObjectPropertyOfAxiom;
import org.semanticweb.elk.owl.interfaces.ElkTransitiveObjectPropertyAxiom;
import org.semanticweb.elk.owl.predefined.ElkPolarity;
import org.semanticweb.elk.owl.predefined.PredefinedElkClass;
import org.semanticweb.elk.reasoner.indexing.caching.CachedIndexedObjectFactory;
import org.semanticweb.elk.reasoner.indexing.caching.ModifiableIndexedObjectCache;
import org.semanticweb.elk.reasoner.indexing.caching.ResolvingModifiableIndexedObjectFactory;
import org.semanticweb.elk.reasoner.indexing.caching.UpdatingModifiableIndexedObjectFactory;
import org.semanticweb.elk.reasoner.indexing.factories.ModifiableIndexedAxiomFactory;
import org.semanticweb.elk.reasoner.indexing.factories.ModifiableIndexedObjectFactory;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedAxiom;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObject;
import org.semanticweb.elk.reasoner.indexing.implementation.ModifiableIndexedObjectFactoryImpl;
import org.semanticweb.elk.reasoner.indexing.inferences.ModifiableIndexedAxiomInferenceFactory;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedAxiom;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClass;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClassExpressionList;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedIndividual;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedObject;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedObjectIntersectionOf;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedObjectProperty;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedPropertyChain;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableOntologyIndex;
import org.semanticweb.elk.reasoner.indexing.modifiable.OccurrenceIncrement;
import org.semanticweb.elk.util.logging.LogLevel;
import org.semanticweb.elk.util.logging.LoggerWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link ElkAxiomConverter} that converts {@link ElkAxiom}
 * s to {@link ModifiableIndexedAxiom}s using a
 * {@link ModifiableIndexedAxiomFactory} in addition to converters and factories
 * for other types of {@link IndexedObject}s.
 * 
 * @author Frantisek Simancik
 * @author "Yevgeny Kazakov"
 * 
 */
public class ElkAxiomConverterImpl extends FailingElkAxiomConverter {

	// logger for events
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(ElkAxiomConverterImpl.class);

	/**
	 * The factory to convert axioms
	 */
	private final ModifiableIndexedAxiomInferenceFactory axiomFactory_;

	/**
	 * The factories to create objects of respectively positive and negative
	 * polarities of occurrences.
	 */
	private final ModifiableIndexedObjectFactory positiveFactory_,
			negativeFactory_;

	private final ElkPolarityExpressionConverter positiveConverter_,
			negativeConverter_, dualConverter_;

	private final ElkEntityConverter entityConverter_;

	ElkAxiomConverterImpl(ModifiableIndexedAxiomInferenceFactory axiomFactory,
			ModifiableIndexedObjectFactory positiveFactory,
			ModifiableIndexedObjectFactory negativeFactory,
			ElkPolarityExpressionConverter positiveConverter,
			ElkPolarityExpressionConverter negativeConverter,
			ElkPolarityExpressionConverter dualConverter,
			ElkEntityConverter entityConverter) {
		this.axiomFactory_ = axiomFactory;
		this.positiveFactory_ = positiveFactory;
		this.negativeFactory_ = negativeFactory;
		this.positiveConverter_ = positiveConverter;
		this.negativeConverter_ = negativeConverter;
		this.dualConverter_ = dualConverter;
		this.entityConverter_ = entityConverter;
	}

	/**
	 * Creates an {@link ElkAxiomConverter} that uses four
	 * {@link ModifiableIndexedObjectFactory}s to create all
	 * {@link ModifiableIndexedObject} sub-expression of the resulting
	 * {@link ModifiableIndexedAxiom}s depending on (logical) polarity of their
	 * occurrence.
	 * 
	 * @param neutralFactory
	 *            this {@link ModifiableIndexedObjectFactory} is used to create
	 *            {@link IndexedAxiom}s as well as
	 *            {@link ModifiableIndexedObject}s that do not occur (logically)
	 *            positively or negatively in the ontology, such as entities in
	 *            declaration axioms
	 * @param positiveFactory
	 *            this {@link ModifiableIndexedObjectFactory} is used to create
	 *            positive occurrences of {@link ModifiableIndexedObject}s, such
	 *            as {@link IndexedClassExpression}s on the right-hand-side of
	 *            {@code SubClassOf} axioms.
	 * 
	 * @param negativeFactory
	 *            this {@link ModifiableIndexedObjectFactory} is used to create
	 *            negative occurrences of {@link ModifiableIndexedObject}s, such
	 *            as {@link IndexedClassExpression}s on the right-hand-side of
	 *            {@code SubClassOf} axioms.
	 * @param dualFactory
	 *            this {@link ModifiableIndexedObjectFactory} is used to create
	 *            simultaneously positive and negative occurrences of
	 *            {@link ModifiableIndexedObject}s, such as
	 *            {@link IndexedClassExpression}s in {@code EquivalentClasses}
	 *            axioms.
	 */
	public ElkAxiomConverterImpl(ModifiableIndexedObjectFactory neutralFactory,
			ModifiableIndexedObjectFactory positiveFactory,
			ModifiableIndexedObjectFactory negativeFactory,
			ModifiableIndexedObjectFactory dualFactory) {
		this.axiomFactory_ = neutralFactory;
		this.positiveFactory_ = positiveFactory;
		this.negativeFactory_ = negativeFactory;
		this.positiveConverter_ = new ElkPolarityExpressionConverterImpl(
				ElkPolarity.POSITIVE, positiveFactory, negativeFactory);
		this.negativeConverter_ = positiveConverter_
				.getComplementaryConverter();
		this.dualConverter_ = new ElkPolarityExpressionConverterImpl(
				dualFactory);
		this.entityConverter_ = new ElkEntityConverterImpl(neutralFactory);
	}

	/**
	 * Creates an {@link ElkAxiomConverter} that uses the given
	 * {@link ModifiableIndexedObjectFactory}s to create all
	 * {@link ModifiableIndexedObject} sub-expression of the resulting
	 * {@link ModifiableIndexedAxiom}s.
	 * 
	 * @param faactory
	 *            the {@link ModifiableIndexedObjectFactory} that is used to
	 *            create all {@link ModifiableIndexedObject}s necessary for
	 *            conversion of {@link ElkAxiom}s.
	 */
	public ElkAxiomConverterImpl(ModifiableIndexedObjectFactory factory) {
		this(factory, factory, factory, factory);
	}

	/**
	 * Creates an {@link ElkAxiomConverter} for converting {@link ElkAxiom}s
	 * that have already been indexed in the given
	 * {@link ModifiableIndexedObjectCache}. All {@link ModifiableIndexedObject}
	 * s subexpression of the converted {@link ModifiabledIndexedAxiom}s are
	 * taken from this {@link ModifiableIndexedObjectCache} as well. The
	 * provided {@link ModifiableIndexedObjectCache} will not change. The
	 * converter may not work properly if used with {@link ElkAxiom}s that have
	 * not been indexed in the given {@link ModifiableIndexedObjectCache} (e.g.,
	 * {@link NullPointerException}s may occur).
	 * 
	 * @param cache
	 *            the {@link ModifiableIndexedObjectCache} from which all
	 *            {@link ModifiableIndexedObject}s are used.
	 */
	public ElkAxiomConverterImpl(ModifiableIndexedObjectCache cache) {
		this(new ResolvingModifiableIndexedObjectFactory(cache));
	}

	<F extends CachedIndexedObjectFactory & ModifiableIndexedObjectFactory> ElkAxiomConverterImpl(
			F baseFactory, ModifiableOntologyIndex index, int increment) {
		this(new UpdatingModifiableIndexedObjectFactory(baseFactory, index,
				OccurrenceIncrement.getNeutralIncrement(increment)),
				new UpdatingModifiableIndexedObjectFactory(baseFactory, index,
						OccurrenceIncrement.getPositiveIncrement(increment)),
				new UpdatingModifiableIndexedObjectFactory(baseFactory, index,
						OccurrenceIncrement.getNegativeIncrement(increment)),
				new UpdatingModifiableIndexedObjectFactory(baseFactory, index,
						OccurrenceIncrement.getDualIncrement(increment)));
	}

	/**
	 * Creates an {@link ElkAxiomConverter} that converts {@link ElkAxiom}s and
	 * adds them to the provided {@link ModifiableOntologyIndex} with the given
	 * multiplicity. The converter will also insert all intermediate
	 * {@link ModifiableIndexedObject} subexpressions of the created
	 * {@link ModifiableIndexedAxiom}s with the same multiplicity for polarity
	 * of their occurrences. Some {@link ModifiableIndexedObject}s will be
	 * reused and only their multiplicity will be updated.
	 * 
	 * @param index
	 *            the {@link ModifiableOntologyIndex} representing the ontology
	 *            to which the resulting {@link ModifiableIndexedObject}s will
	 *            be added and processed (compiled to rules).
	 * @param increment
	 *            the multiplicity with which the created
	 *            {@link ModifiableIndexedAxiom}s should be inserted to the
	 *            {@link ModifiableOntologyIndex} (e.g., 2 means 2 copies of
	 *            each create axiom should be added); it can be negative, which
	 *            means that the {@link ModifiableIndexedAxiom}s should be
	 *            removed the corresponding number of times.
	 * 
	 */
	public ElkAxiomConverterImpl(ModifiableOntologyIndex index, int increment) {
		this(new ModifiableIndexedObjectFactoryImpl(), index, increment);
	}

	@Override
	public Void visit(ElkObjectPropertyDomainAxiom axiom) {
		ModifiableIndexedClass indexedOwlThing = negativeFactory_
				.getIndexedClass(PredefinedElkClass.OWL_THING);
		axiomFactory_.getElkObjectPropertyDomainAxiomConversion(axiom,
				negativeFactory_.getIndexedObjectSomeValuesFrom(
						axiom.getProperty().accept(negativeConverter_),
						indexedOwlThing),
				axiom.getDomain().accept(positiveConverter_));
		return null;
	}

	@Override
	public Void visit(ElkObjectPropertyRangeAxiom axiom) {
		axiomFactory_.getElkObjectPropertyRangeAxiomConversion(axiom,
				axiom.getProperty().accept(negativeConverter_),
				axiom.getRange().accept(positiveConverter_));
		return null;
	}

	@Override
	public Void visit(ElkReflexiveObjectPropertyAxiom axiom) {
		axiomFactory_.getElkReflexiveObjectPropertyAxiomConversion(axiom,
				negativeFactory_.getIndexedClass(PredefinedElkClass.OWL_THING),
				positiveFactory_.getIndexedObjectHasSelf(
						axiom.getProperty().accept(positiveConverter_)));
		return null;
	}

	@Override
	public Void visit(ElkSubObjectPropertyOfAxiom axiom) {
		axiomFactory_.getElkSubObjectPropertyOfAxiomConversion(axiom,
				axiom.getSubObjectPropertyExpression().accept(this),
				axiom.getSuperObjectPropertyExpression()
						.accept(positiveConverter_));
		return null;
	}

	@Override
	public Void visit(ElkEquivalentObjectPropertiesAxiom axiom) {
		// reduces equivalence to sub-property axioms
		ModifiableIndexedObjectProperty first = null;
		List<? extends ElkObjectPropertyExpression> members = axiom
				.getObjectPropertyExpressions();
		for (int pos = 0; pos < members.size(); pos++) {
			ElkObjectPropertyExpression pe = members.get(pos);
			if (first == null)
				first = pe.accept(dualConverter_);
			else {
				ModifiableIndexedObjectProperty other = pe
						.accept(dualConverter_);
				axiomFactory_.getElkEquivalentObjectPropertiesAxiomConversion(
						axiom, 0, pos, first, other);
				axiomFactory_.getElkEquivalentObjectPropertiesAxiomConversion(
						axiom, pos, 0, other, first);
			}
		}
		return null;
	}

	@Override
	public Void visit(ElkTransitiveObjectPropertyAxiom axiom) {
		ModifiableIndexedObjectProperty p = axiom.getProperty()
				.accept(dualConverter_);
		axiomFactory_.getElkTransitiveObjectPropertyAxiomConversion(axiom,
				negativeFactory_.getIndexedComplexPropertyChain(p, p), p);
		return null;
	}

	@Override
	public Void visit(ElkEquivalentClassesAxiom axiom) {
		// reduces equivalence to class definitions or sub-class axioms
		ModifiableIndexedClassExpression first = null;
		List<? extends ElkClassExpression> members = axiom
				.getClassExpressions();
		for (int pos = 0; pos < members.size(); pos++) {
			ElkClassExpression c = members.get(pos);
			if (first == null)
				first = c.accept(dualConverter_);
			else {
				ModifiableIndexedClassExpression other = c
						.accept(dualConverter_);
				if (first instanceof ModifiableIndexedClass) {
					axiomFactory_
							.getElkEquivalentClassesAxiomDefinitionConversion(
									axiom, 0, pos,
									(ModifiableIndexedClass) first, other);
				} else if (other instanceof ModifiableIndexedClass) {
					axiomFactory_
							.getElkEquivalentClassesAxiomDefinitionConversion(
									axiom, pos, 0,
									(ModifiableIndexedClass) other, first);
				} else {
					// index as two subsumptions
					axiomFactory_
							.getElkEquivalentClassesAxiomSubClassConversion(
									axiom, 0, pos, first, other);
					axiomFactory_
							.getElkEquivalentClassesAxiomSubClassConversion(
									axiom, pos, 0, other, first);
				}
			}
		}
		return null;
	}

	@Override
	public Void visit(ElkSameIndividualAxiom axiom) {
		// reduces equivalence to sub-class axioms
		ModifiableIndexedIndividual first = null;
		List<? extends ElkIndividual> members = axiom.getIndividuals();
		for (int pos = 0; pos < members.size(); pos++) {
			ElkIndividual ind = members.get(pos);
			if (first == null) {
				first = ind.accept(dualConverter_);
			} else {
				ModifiableIndexedIndividual other = ind
						.accept(dualConverter_);
				axiomFactory_.getElkSameIndividualAxiomConversion(axiom, 0,
						pos, first, other);
				axiomFactory_.getElkSameIndividualAxiomConversion(axiom, pos,
						0, other, first);
			}
		}
		return null;
	}

	@Override
	public Void visit(ElkSubClassOfAxiom axiom) {
		axiomFactory_.getElkSubClassOfAxiomConversion(axiom,
				axiom.getSubClassExpression().accept(negativeConverter_),
				axiom.getSuperClassExpression().accept(positiveConverter_));
		return null;
	}

	/**
	 * With at most how many disjoint classes transform to binary disjointness
	 * axioms
	 */
	private final static int DISJOINT_AXIOM_BINARIZATION_THRESHOLD = 2;

	private interface DisjointnessIndexer<A extends ElkAxiom, M extends ElkObject> {

		ModifiableIndexedClassExpression convert(M member);

		void indexNary(A axiom, ModifiableIndexedClassExpressionList members);

		void indexBinary(A axiom, int firstMemberPosition,
				int secondMemberPosition,
				ModifiableIndexedObjectIntersectionOf memberConunction,
				ModifiableIndexedClass bottom);
	}

	private <A extends ElkAxiom, M extends ElkObject> void indexDisjointMembers(
			DisjointnessIndexer<A, M> indexer, A axiom,
			List<? extends M> members) {
		/*
		 * for many disjoint members, convert natively
		 */
		if (members.size() > DISJOINT_AXIOM_BINARIZATION_THRESHOLD) {
			List<ModifiableIndexedClassExpression> indexedMembers = new ArrayList<ModifiableIndexedClassExpression>(
					members.size());
			for (M member : members)
				indexedMembers.add(indexer.convert(member));
			indexer.indexNary(axiom, positiveFactory_
					.getIndexedClassExpressionList(indexedMembers));
			return;
		}
		/*
		 * otherwise create a binary disjointness axioms for all pairs (first,
		 * second) where second occurs after first in members
		 */
		ModifiableIndexedClass indexedOwlNothing = positiveFactory_
				.getIndexedClass(PredefinedElkClass.OWL_NOTHING);
		for (int firstPos = 0; firstPos < members.size(); firstPos++) {
			M first = members.get(firstPos);
			ModifiableIndexedClassExpression firstIndexed = indexer
					.convert(first);
			boolean selfFound = false;
			for (int secondPos = 0; secondPos < members.size(); secondPos++) {
				M second = members.get(secondPos);
				if (!selfFound) {
					if (second == first)
						selfFound = true;
					continue;
				}
				// else second appears after first
				ModifiableIndexedClassExpression secondIndexed = indexer
						.convert(second);
				indexer.indexBinary(axiom, firstPos, secondPos,
						negativeFactory_.getIndexedObjectIntersectionOf(
								firstIndexed, secondIndexed),
						indexedOwlNothing);
			}
		}
	}

	private class ElkDisjointClassesAxiomDisjointnessIndexer
			implements
				DisjointnessIndexer<ElkDisjointClassesAxiom, ElkClassExpression> {

		@Override
		public ModifiableIndexedClassExpression convert(
				ElkClassExpression member) {
			return member.accept(negativeConverter_);
		}

		@Override
		public void indexNary(ElkDisjointClassesAxiom axiom,
				ModifiableIndexedClassExpressionList members) {
			axiomFactory_.getElkDisjointClassesAxiomNaryConversion(axiom,
					members);
		}

		@Override
		public void indexBinary(ElkDisjointClassesAxiom axiom,
				int firstMemberPosition, int secondMemberPosition,
				ModifiableIndexedObjectIntersectionOf memberConunction,
				ModifiableIndexedClass bottom) {
			axiomFactory_.getElkDisjointClassesAxiomBinaryConversion(axiom,
					firstMemberPosition, secondMemberPosition, memberConunction,
					bottom);
		}
	}

	private final ElkDisjointClassesAxiomDisjointnessIndexer elkDisjointClassesAxiomDisjointnessIndexer = new ElkDisjointClassesAxiomDisjointnessIndexer();

	@Override
	public Void visit(ElkDisjointClassesAxiom axiom) {
		indexDisjointMembers(elkDisjointClassesAxiomDisjointnessIndexer, axiom,
				axiom.getClassExpressions());
		return null;
	}

	private class ElkDisjointUnionAxiomDisjointnessIndexer
			implements
				DisjointnessIndexer<ElkDisjointUnionAxiom, ElkClassExpression> {

		@Override
		public ModifiableIndexedClassExpression convert(
				ElkClassExpression member) {
			return member.accept(negativeConverter_);
		}

		@Override
		public void indexNary(ElkDisjointUnionAxiom axiom,
				ModifiableIndexedClassExpressionList members) {
			axiomFactory_.getElkDisjointUnionAxiomNaryConversion(axiom,
					members);
		}

		@Override
		public void indexBinary(ElkDisjointUnionAxiom axiom,
				int firstMemberPosition, int secondMemberPosition,
				ModifiableIndexedObjectIntersectionOf memberConunction,
				ModifiableIndexedClass bottom) {
			axiomFactory_.getElkDisjointUnionAxiomBinaryConversion(axiom,
					firstMemberPosition, secondMemberPosition, memberConunction,
					bottom);
		}

	}

	private final ElkDisjointUnionAxiomDisjointnessIndexer elkDisjointUnionAxiomDisjointnessIndexer = new ElkDisjointUnionAxiomDisjointnessIndexer();

	@Override
	public Void visit(ElkDisjointUnionAxiom axiom) {
		List<? extends ElkClassExpression> disjoint = axiom
				.getClassExpressions();
		indexDisjointMembers(elkDisjointUnionAxiomDisjointnessIndexer, axiom,
				disjoint);
		ModifiableIndexedClass defined;
		ModifiableIndexedClassExpression member;
		int size = disjoint.size();
		switch (size) {
			case 0 :
				defined = (ModifiableIndexedClass) axiom.getDefinedClass()
						.accept(positiveConverter_);
				member = negativeFactory_
						.getIndexedClass(PredefinedElkClass.OWL_THING);
				axiomFactory_.getElkDisjointUnionAxiomSubClassConversion(axiom,
						0, member, defined);
				break;
			case 1 :
				defined = (ModifiableIndexedClass) axiom.getDefinedClass()
						.accept(dualConverter_);
				member = disjoint.iterator().next().accept(dualConverter_);
				axiomFactory_.getElkDisjointUnionAxiomDefinitionConversion(
						axiom, defined, member);
				break;
			default :
				// indexing only one direction of the equivalence
				if (LOGGER_.isWarnEnabled()) {
					LoggerWrap.log(LOGGER_, LogLevel.WARN,
							"reasoner.indexing.disjointUnion",
							"ELK supports DisjointUnion only partially. Reasoning might be incomplete!");
				}
				defined = (ModifiableIndexedClass) axiom.getDefinedClass()
						.accept(positiveConverter_);
				for (int pos = 0; pos < size; pos++) {
					ElkClassExpression c = disjoint.get(pos);
					member = c.accept(negativeConverter_);
					axiomFactory_.getElkDisjointUnionAxiomSubClassConversion(
							axiom, pos, member, defined);
				}
				break;
		}
		return null;
	}

	private class ElkDifferentIndividualsAxiomDisjointnessIndexer
			implements
				DisjointnessIndexer<ElkDifferentIndividualsAxiom, ElkIndividual> {

		@Override
		public ModifiableIndexedClassExpression convert(ElkIndividual member) {
			return member.accept(negativeConverter_);
		}

		@Override
		public void indexNary(ElkDifferentIndividualsAxiom axiom,
				ModifiableIndexedClassExpressionList members) {
			axiomFactory_.getElkDifferentIndividualsAxiomNaryConversion(axiom,
					members);
		}

		@Override
		public void indexBinary(ElkDifferentIndividualsAxiom axiom,
				int firstMemberPosition, int secondMemberPosition,
				ModifiableIndexedObjectIntersectionOf memberConunction,
				ModifiableIndexedClass bottom) {
			axiomFactory_.getElkDifferentIndividualsAxiomBinaryConversion(axiom,
					firstMemberPosition, secondMemberPosition, memberConunction,
					bottom);
		}
	}

	private final ElkDifferentIndividualsAxiomDisjointnessIndexer elkDifferentIndividualsAxiomDisjointnessIndexer = new ElkDifferentIndividualsAxiomDisjointnessIndexer();

	@Override
	public Void visit(ElkDifferentIndividualsAxiom axiom) {
		indexDisjointMembers(elkDifferentIndividualsAxiomDisjointnessIndexer,
				axiom, axiom.getIndividuals());
		return null;
	}

	@Override
	public Void visit(ElkClassAssertionAxiom axiom) {
		axiomFactory_.getElkClassAssertionAxiomConversion(axiom,
				axiom.getIndividual().accept(negativeConverter_),
				axiom.getClassExpression().accept(positiveConverter_));
		return null;
	}

	@Override
	public Void visit(ElkObjectPropertyAssertionAxiom axiom) {
		axiomFactory_.getElkObjectPropertyAssertionAxiomConversion(axiom,
				axiom.getSubject().accept(negativeConverter_),
				positiveFactory_.getIndexedObjectSomeValuesFrom(
						axiom.getProperty().accept(positiveConverter_),
						axiom.getObject().accept(positiveConverter_)));
		return null;
	}

	@Override
	public Void visit(ElkDeclarationAxiom axiom) {
		axiomFactory_.getElkDeclarationAxiomConversion(axiom,
				axiom.getEntity().accept(entityConverter_));
		return null;
	}

	@Override
	public ModifiableIndexedPropertyChain visit(ElkObjectProperty expression) {
		return expression.accept(negativeConverter_);
	}

	@Override
	public ModifiableIndexedPropertyChain visit(
			ElkObjectPropertyChain elkObjectPropertyChain) {
		ModifiableIndexedPropertyChain result = null;
		// iterating over the chain backwards
		ListIterator<? extends ElkObjectPropertyExpression> iterator = elkObjectPropertyChain
				.getObjectPropertyExpressions()
				.listIterator(elkObjectPropertyChain
						.getObjectPropertyExpressions().size());
		while (iterator.hasPrevious()) {
			ModifiableIndexedObjectProperty iop = iterator.previous()
					.accept(negativeConverter_);
			if (result == null) {
				result = iop;
				continue;
			}
			result = negativeFactory_.getIndexedComplexPropertyChain(iop,
					result);
		}
		return result;
	}

}
