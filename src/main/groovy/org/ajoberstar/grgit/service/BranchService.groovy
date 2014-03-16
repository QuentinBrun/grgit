/*
 * Copyright 2012-2013 the original author or authors.
 *
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
 */
package org.ajoberstar.grgit.service

import org.ajoberstar.grgit.Branch
import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.operation.BranchAddOp
import org.ajoberstar.grgit.operation.BranchListOp
// import org.ajoberstar.grgit.operation.BranchRemoveOp
import org.ajoberstar.grgit.util.OpSyntaxUtil

class BranchService {
	private static final Map OPERATIONS = [
		list: BranchListOp, add: BranchAddOp/*, remote: BranchRemoveOp*/]
	final Repository repository

	BranchService(Repository repository) {
		this.repository = repository
	}

	def methodMissing(String name, args) {
		OpSyntaxUtil.tryOp(this.class, OPERATIONS, [repository] as Object[], name, args)
	}
}