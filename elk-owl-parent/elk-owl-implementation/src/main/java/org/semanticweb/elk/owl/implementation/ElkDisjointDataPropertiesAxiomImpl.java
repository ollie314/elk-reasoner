/*
 * #%L
 * ELK Reasoner
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
package org.semanticweb.elk.owl.implementation;

import java.util.List;

import org.semanticweb.elk.owl.interfaces.ElkDisjointDataPropertiesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDataPropertyExpression;
import org.semanticweb.elk.owl.visitors.ElkAxiomVisitor;
import org.semanticweb.elk.owl.visitors.ElkDataPropertyAxiomVisitor;
import org.semanticweb.elk.owl.visitors.ElkObjectVisitor;

/**
 * Corresponds to an <a href=
 * "http://www.w3.org/TR/owl2-syntax/#Disjoint_Data_Properties">Disjoint
 * Data Properties Axiom<a> in the OWL 2 specification.
 * 
 * @author Markus Kroetzsch
 */
public class ElkDisjointDataPropertiesAxiomImpl extends
		ElkDataPropertyExpressionListObject implements
		ElkDisjointDataPropertiesAxiom {

	private static final int constructorHash_ = "ElkDisjointDataPropertiesAxiom"
			.hashCode();

	/* package-private */ElkDisjointDataPropertiesAxiomImpl(
			List<? extends ElkDataPropertyExpression> disjointDataPropertyExpressions) {
		super(disjointDataPropertyExpressions);
		this.structuralHashCode = ElkObjectImpl.computeCompositeHash(
				constructorHash_, disjointDataPropertyExpressions);
	}

	@Override
	public String toString() {
		return buildFssString("DisjointDataProperties");
	}

	public boolean structuralEquals(Object object) {
		if (this == object) {
			return true;
		} else if (object instanceof ElkDisjointDataPropertiesAxiom) {
			return elkObjects
					.equals(((ElkDisjointDataPropertiesAxiom) object)
							.getDataPropertyExpressions());
		} else {
			return false;
		}
	}

	public <O> O accept(ElkDataPropertyAxiomVisitor<O> visitor) {
		return visitor.visit(this);
	}

	public <O> O accept(ElkAxiomVisitor<O> visitor) {
		return visitor.visit(this);
	}

	public <O> O accept(ElkObjectVisitor<O> visitor) {
		return visitor.visit(this);
	}

}