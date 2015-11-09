package org.semanticweb.elk.reasoner.indexing.implementation;

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

import org.semanticweb.elk.owl.interfaces.ElkDeclarationAxiom;
import org.semanticweb.elk.reasoner.indexing.inferences.IndexedDeclarationAxiomInferenceVisitor;
import org.semanticweb.elk.reasoner.indexing.inferences.ModifiableElkDeclarationAxiomConversion;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedEntity;

/**
 * Implements {@link ModifiableElkDeclarationAxiomConversion}
 * 
 * @author "Yevgeny Kazakov"
 */
class ModifiableElkDeclarationAxiomConversionImpl
		extends
			ModifiableIndexedDeclarationAxiomInferenceImpl<ElkDeclarationAxiom>
		implements
			ModifiableElkDeclarationAxiomConversion {

	ModifiableElkDeclarationAxiomConversionImpl(
			ElkDeclarationAxiom originalAxiom, ModifiableIndexedEntity entity) {
		super(originalAxiom, entity);
	}

	@Override
	public <I, O> O accept(
			IndexedDeclarationAxiomInferenceVisitor<I, O> visitor, I input) {
		return visitor.visit(this, input);
	}

}