/*
 * Copyright 2012-2014 the original author or authors.
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
package org.ajoberstar.grgit.operation

import spock.lang.Unroll

import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Person
import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.exception.GrgitException
import org.ajoberstar.grgit.fixtures.GitTestUtil
import org.ajoberstar.grgit.fixtures.MultiGitOpSpec
import org.ajoberstar.grgit.service.RepositoryService
import org.ajoberstar.grgit.util.JGitUtil

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand.ListMode

import org.junit.Rule
import org.junit.rules.TemporaryFolder

class PushOpSpec extends MultiGitOpSpec {
	RepositoryService localGrgit
	RepositoryService remoteGrgit

	def setup() {
		// TODO: Conver to Grgit after branch and tag

		remoteGrgit = init('remote')

		repoFile(remoteGrgit, '1.txt') << '1'
		remoteGrgit.commit(message: 'do', all: true)

		remoteGrgit.repository.git.branchCreate().with {
			name = 'my-branch'
			delegate.call()
		}

		localGrgit = clone('local', remoteGrgit)
		localGrgit.checkout(branch: 'my-branch', createBranch: true)

		repoFile(localGrgit, '1.txt') << '1.5'
		localGrgit.commit(message: 'do', all: true)

		localGrgit.repository.git.tag().with {
			name = 'tag1'
			delegate.call()
		}

		localGrgit.checkout(branch: 'master')

		repoFile(localGrgit, '1.txt') << '2'
		localGrgit.commit(message: 'do', all: true)

		localGrgit.repository.git.tag().with {
			name = 'tag2'
			delegate.call()
		}
	}

	def 'push to non-existent remote fails'() {
		when:
		localGrgit.push(remote: 'fake')
		then:
		thrown(GrgitException)
	}

	def 'push without other settings pushes correct commits'() {
		when:
		localGrgit.push()
		then:
		GitTestUtil.resolve(localGrgit, 'refs/heads/master') == GitTestUtil.resolve(remoteGrgit, 'refs/heads/master')
		GitTestUtil.resolve(localGrgit, 'refs/heads/my-branch') != GitTestUtil.resolve(remoteGrgit, 'refs/heads/my-branch')
		!GitTestUtil.tags(remoteGrgit)
	}

	def 'push with all true pushes all branches'() {
		when:
		localGrgit.push(all: true)
		then:
		GitTestUtil.resolve(localGrgit, 'refs/heads/master') == GitTestUtil.resolve(remoteGrgit, 'refs/heads/master')
		GitTestUtil.resolve(localGrgit, 'refs/heads/my-branch') == GitTestUtil.resolve(remoteGrgit, 'refs/heads/my-branch')
		!GitTestUtil.tags(remoteGrgit)
	}

	def 'push with tags true pushes all tags'() {
		when:
		localGrgit.push(tags: true)
		then:
		GitTestUtil.resolve(localGrgit, 'refs/heads/master') != GitTestUtil.resolve(remoteGrgit, 'refs/heads/master')
		GitTestUtil.resolve(localGrgit, 'refs/heads/my-branch') != GitTestUtil.resolve(remoteGrgit, 'refs/heads/my-branch')
		GitTestUtil.tags(localGrgit) == GitTestUtil.tags(remoteGrgit)
	}

	def 'push with refs only pushes those refs'() {
		when:
		localGrgit.push(refsOrSpecs: ['my-branch'])
		then:
		GitTestUtil.resolve(localGrgit, 'refs/heads/master') != GitTestUtil.resolve(remoteGrgit, 'refs/heads/master')
		GitTestUtil.resolve(localGrgit, 'refs/heads/my-branch') == GitTestUtil.resolve(remoteGrgit, 'refs/heads/my-branch')
		!GitTestUtil.tags(remoteGrgit)
	}

	def 'push with refSpecs only pushes those refs'() {
		when:
		localGrgit.push(refsOrSpecs: ['+refs/heads/my-branch:refs/heads/other-branch'])
		then:
		GitTestUtil.resolve(localGrgit, 'refs/heads/master') != GitTestUtil.resolve(remoteGrgit, 'refs/heads/master')
		GitTestUtil.resolve(localGrgit, 'refs/heads/my-branch') != GitTestUtil.resolve(remoteGrgit, 'refs/heads/my-branch')
		GitTestUtil.resolve(localGrgit, 'refs/heads/my-branch') == GitTestUtil.resolve(remoteGrgit, 'refs/heads/other-branch')
		!GitTestUtil.tags(remoteGrgit)
	}
}