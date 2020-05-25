/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.git.script;

import java.io.File;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.gitective.core.stat.UserCommitActivity;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.xwiki.environment.Environment;
import org.xwiki.environment.internal.StandardEnvironment;
import org.xwiki.git.GitHelper;
import org.xwiki.script.service.ScriptService;
import org.xwiki.test.ComponentManagerRule;
import org.xwiki.test.annotation.AllComponents;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link org.xwiki.git.script.GitScriptService}.
 *
 * @version $Id$
 * @since 4.2M1
 */
@AllComponents
public class GitScriptServiceTest
{
    private static final String TEST_REPO_ORIG = "test-repo-orig";

    private static final String TEST_REPO_CLONED = "test-repo-cloned";

    @Rule
    public ComponentManagerRule componentManager = new ComponentManagerRule();
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private File testRepository;

    @Before
    public void setupRepository() throws Exception
    {
        // Configure permanent directory to be the temporary directory
        StandardEnvironment environment = this.componentManager.getInstance(Environment.class);
        environment.setPermanentDirectory(environment.getTemporaryDirectory());
        GitHelper gitHelper = new GitHelper(environment);

        // Delete repositories
        FileUtils.deleteDirectory(gitHelper.getRepositoryFile(TEST_REPO_ORIG));
        FileUtils.deleteDirectory(gitHelper.getRepositoryFile(TEST_REPO_CLONED));

        // Create a Git repository for the test
        this.testRepository = gitHelper.createGitTestRepository(TEST_REPO_ORIG).getDirectory();

        // Add a file so that we can test querying the test repository for more fun!
        gitHelper.add(testRepository, "test.txt", "test content", new PersonIdent("test author", "author@doe.com"),
            new PersonIdent("test committer", "committer@doe.com"), "first commit");
    }

    @Test
    public void getRepositoryAndFindAuthors() throws Exception
    {
        GitScriptService service = this.componentManager.getInstance(ScriptService.class, "git");
        String localPath = this.tmpFolder.newFolder(TEST_REPO_CLONED).toString();
        Repository repository = service.getRepository(this.testRepository.getAbsolutePath(), localPath);
        assertEquals(true, new Git(repository).pull().call().isSuccessful());
        // Now find authors
        Set<PersonIdent> authors = service.findAuthors(repository);
        assertEquals(1, authors.size());
        assertEquals("test author", authors.iterator().next().getName());
    }

    @Test
    public void getRepositoryWithCredentialsAndCountCommits() throws Exception
    {
        GitScriptService service = this.componentManager.getInstance(ScriptService.class, "git");
        String localPath = this.tmpFolder.newFolder(TEST_REPO_CLONED).toString();
        Repository repository = service.getRepository(this.testRepository.getAbsolutePath(), localPath,
            "test author", "TestAccessCode");
        assertEquals(true, new Git(repository).pull().call().isSuccessful());
        // Now count commits
        UserCommitActivity[] commits = service.countAuthorCommits(1, repository);
        // 1 author
        assertEquals(1, commits.length);
        // 1 commit
        assertEquals(1, commits[0].getCount());
    }

    @Test
    public void getRepositoryBare() throws Exception
    {
        GitScriptService service = this.componentManager.getInstance(ScriptService.class, "git");
        CloneCommand cloneCommand = service.createCloneCommand();
        cloneCommand.setBare(true);
        String localPath = this.tmpFolder.newFolder(TEST_REPO_CLONED).toString();
        Repository repository = service.getRepository(this.testRepository.getAbsolutePath(), localPath,
            cloneCommand);
        assertEquals(true, repository.isBare());
        // Now check branch
        assertEquals("master", repository.getBranch());
    }
}
