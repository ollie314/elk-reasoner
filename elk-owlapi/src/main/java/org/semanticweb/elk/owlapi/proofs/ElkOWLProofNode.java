package org.semanticweb.elk.owlapi.proofs;

/*-
 * #%L
 * ELK OWL API Binding
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2016 Department of Computer Science, University of Oxford
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

import java.util.Collection;

import org.liveontologies.owlapi.proof.OWLProofNode;
import org.liveontologies.owlapi.proof.OWLProofStep;
import org.semanticweb.elk.owl.inferences.ElkInference;
import org.semanticweb.elk.owl.inferences.ElkInferenceSet;
import org.semanticweb.elk.owl.inferences.ElkToldAxiom;
import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObject;
import org.semanticweb.elk.owlapi.ElkConverter;
import org.semanticweb.elk.util.collections.Operations;
import org.semanticweb.elk.util.collections.Operations.Transformation;
import org.semanticweb.owlapi.model.OWLAxiom;

public class ElkOWLProofNode implements OWLProofNode {

	private final ElkAxiom member_;

	private final ElkInferenceSet elkInferences_;

	private final ElkObject.Factory elkFactory_;

	private int hash_ = 0;

	public ElkOWLProofNode(ElkAxiom member, ElkInferenceSet elkInferences,
			ElkObject.Factory elkFactory) {
		this.member_ = member;
		this.elkInferences_ = elkInferences;
		this.elkFactory_ = elkFactory;
	}

	@Override
	public OWLAxiom getMember() {
		return ElkConverter.getInstance().convert(member_);
	}

	@Override
	public Collection<? extends OWLProofStep> getInferences() {
		return Operations.map(elkInferences_.get(member_),
				new Transformation<ElkInference, OWLProofStep>() {

					@Override
					public OWLProofStep transform(ElkInference element) {
						if (element instanceof ElkToldAxiom) {
							// don't transform told axioms
							return null;
						}
						// else
						return new ElkOWLProofStep(element, elkInferences_,
								elkFactory_);
					}
				});
	}

	@Override
	public int hashCode() {
		if (hash_ == 0) {
			hash_ = member_.hashCode() + elkInferences_.hashCode();
		}
		return hash_;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (o instanceof ElkOWLProofNode) {
			ElkOWLProofNode other = (ElkOWLProofNode) o;
			return member_.equals(other.member_)
					&& elkInferences_.equals(other.elkInferences_);
		}
		// else
		return false;
	}

	@Override
	public String toString() {
		return member_.toString();
	}

	@Override
	public void addListener(ChangeListener listener) {
		elkInferences_.add(new ListenerDelegator(listener));
	}

	@Override
	public void removeListener(ChangeListener listener) {
		elkInferences_.remove(new ListenerDelegator(listener));
	}

	private static class ListenerDelegator implements ElkInferenceSet.ChangeListener {

		private final OWLProofNode.ChangeListener nodeListener_;

		ListenerDelegator(OWLProofNode.ChangeListener nodeListener) {
			this.nodeListener_ = nodeListener;
		}

		@Override
		public void inferencesChanged() {
			nodeListener_.nodeChanged();
		}

		@Override
		public int hashCode() {
			return nodeListener_.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ListenerDelegator) {
				return nodeListener_
						.equals(((ListenerDelegator) o).nodeListener_);
			}
			// else
			return false;
		}

	}

}
