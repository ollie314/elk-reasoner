package org.semanticweb.elk.reasoner.indexing.inferences;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2015 Department of Computer Science, University of Oxford
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

import org.semanticweb.elk.owl.interfaces.ElkClassAssertionAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDeclarationAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDifferentIndividualsAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDisjointClassesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDisjointUnionAxiom;
import org.semanticweb.elk.owl.interfaces.ElkEquivalentClassesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkEquivalentObjectPropertiesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyAssertionAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyDomainAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyRangeAxiom;
import org.semanticweb.elk.owl.interfaces.ElkReflexiveObjectPropertyAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSameIndividualAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSubClassOfAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSubObjectPropertyOfAxiom;
import org.semanticweb.elk.owl.interfaces.ElkTransitiveObjectPropertyAxiom;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClass;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClassExpressionList;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedEntity;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedIndividual;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedObjectIntersectionOf;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedObjectProperty;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedPropertyChain;

/**
 * A factory for creating instances of {@link ModifiableIndexedAxiomInference}
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
public interface ModifiableIndexedAxiomInferenceFactory {

	ModifiableElkClassAssertionAxiomConversion getElkClassAssertionAxiomConversion(
			ElkClassAssertionAxiom originalAxiom,
			ModifiableIndexedIndividual instance,
			ModifiableIndexedClassExpression type);

	ModifiableElkDeclarationAxiomConversion getElkDeclarationAxiomConversion(
			ElkDeclarationAxiom originalAxiom, ModifiableIndexedEntity entity);

	ModifiableElkDifferentIndividualsAxiomBinaryConversion getElkDifferentIndividualsAxiomBinaryConversion(
			ElkDifferentIndividualsAxiom originalAxiom,
			int firstIndividualPosition, int secondIndividualPosition,
			ModifiableIndexedObjectIntersectionOf conjunction,
			ModifiableIndexedClass bottom);

	ModifiableElkDifferentIndividualsAxiomNaryConversion getElkDifferentIndividualsAxiomNaryConversion(
			ElkDifferentIndividualsAxiom originalAxiom,
			ModifiableIndexedClassExpressionList differentIndividuals);

	ModifiableElkDisjointClassesAxiomBinaryConversion getElkDisjointClassesAxiomBinaryConversion(
			ElkDisjointClassesAxiom originalAxiom, int firstClassPosition,
			int secondClassPosition,
			ModifiableIndexedObjectIntersectionOf conjunction,
			ModifiableIndexedClass bottom);

	ModifiableElkDisjointClassesAxiomNaryConversion getElkDisjointClassesAxiomNaryConversion(
			ElkDisjointClassesAxiom originalAxiom,
			ModifiableIndexedClassExpressionList disjointClasses);

	ModifiableElkDisjointUnionAxiomBinaryConversion getElkDisjointUnionAxiomBinaryConversion(
			ElkDisjointUnionAxiom originalAxiom, int firstDisjunctPosition,
			int secondDisjunctPosition,
			ModifiableIndexedObjectIntersectionOf conjunction,
			ModifiableIndexedClass bottom);

	ModifiableElkDisjointUnionAxiomDefinitionConversion getElkDisjointUnionAxiomDefinitionConversion(
			ElkDisjointUnionAxiom originalAxiom,
			ModifiableIndexedClass definedClass,
			ModifiableIndexedClassExpression definition);

	ModifiableElkDisjointUnionAxiomNaryConversion getElkDisjointUnionAxiomNaryConversion(
			ElkDisjointUnionAxiom originalAxiom,
			ModifiableIndexedClassExpressionList disjointClasses);

	ModifiableElkDisjointUnionAxiomSubClassConversion getElkDisjointUnionAxiomSubClassConversion(
			ElkDisjointUnionAxiom originalAxiom, int disjunctPosition,
			ModifiableIndexedClassExpression disjunct,
			ModifiableIndexedClass definedClass);

	ModifiableElkEquivalentClassesAxiomDefinitionConversion getElkEquivalentClassesAxiomDefinitionConversion(
			ElkEquivalentClassesAxiom originalAxiom, int definedClassPosition,
			int definitionPosition, ModifiableIndexedClass definedClass,
			ModifiableIndexedClassExpression definition);

	ModifiableElkEquivalentClassesAxiomSubClassConversion getElkEquivalentClassesAxiomSubClassConversion(
			ElkEquivalentClassesAxiom originalAxiom, int subClassPosition,
			int superClassPosition, ModifiableIndexedClassExpression subClass,
			ModifiableIndexedClassExpression superClass);

	ModifiableElkEquivalentObjectPropertiesAxiomConversion getElkEquivalentObjectPropertiesAxiomConversion(
			ElkEquivalentObjectPropertiesAxiom originalAxiom,
			int subPropertyPosition, int superPropertyPosition,
			ModifiableIndexedObjectProperty subProperty,
			ModifiableIndexedObjectProperty superProperty);

	ModifiableElkObjectPropertyAssertionAxiomConversion getElkObjectPropertyAssertionAxiomConversion(
			ElkObjectPropertyAssertionAxiom originalAxiom,
			ModifiableIndexedClassExpression subClass,
			ModifiableIndexedClassExpression superClass);

	ModifiableElkObjectPropertyDomainAxiomConversion getElkObjectPropertyDomainAxiomConversion(
			ElkObjectPropertyDomainAxiom originalAxiom,
			ModifiableIndexedClassExpression subClass,
			ModifiableIndexedClassExpression superClass);

	ModifiableElkObjectPropertyRangeAxiomConversion getElkObjectPropertyRangeAxiomConversion(
			ElkObjectPropertyRangeAxiom originalAxiom,
			ModifiableIndexedObjectProperty property,
			ModifiableIndexedClassExpression range);

	ModifiableElkReflexiveObjectPropertyAxiomConversion getElkReflexiveObjectPropertyAxiomConversion(
			ElkReflexiveObjectPropertyAxiom originalAxiom,
			ModifiableIndexedClassExpression subClass,
			ModifiableIndexedClassExpression superClass);

	ModifiableElkSameIndividualAxiomConversion getElkSameIndividualAxiomConversion(
			ElkSameIndividualAxiom originalAxiom, int subIndividualPosition,
			int superIndividualPosition,
			ModifiableIndexedIndividual subIndividual,
			ModifiableIndexedIndividual superIndividual);

	ModifiableElkSubClassOfAxiomConversion getElkSubClassOfAxiomConversion(
			ElkSubClassOfAxiom originalAxiom,
			ModifiableIndexedClassExpression subClass,
			ModifiableIndexedClassExpression superClass);

	ModifiableElkSubObjectPropertyOfAxiomConversion getElkSubObjectPropertyOfAxiomConversion(
			ElkSubObjectPropertyOfAxiom originalAxiom,
			ModifiableIndexedPropertyChain subPropertyChain,
			ModifiableIndexedObjectProperty superProperty);

	ModifiableElkTransitiveObjectPropertyAxiomConversion getElkTransitiveObjectPropertyAxiomConversion(
			ElkTransitiveObjectPropertyAxiom originalAxiom,
			ModifiableIndexedPropertyChain subPropertyChain,
			ModifiableIndexedObjectProperty superProperty);

}
