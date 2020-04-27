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
package org.xwiki.git.internal;

import java.io.File;
import java.net.URI;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.junit.TestRepository;
import org.eclipse.jgit.junit.http.AppServer;
import org.eclipse.jgit.junit.http.HttpTestCase;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link DefaultGitManager}
 *
 * @version $Id$
 * @since 9.9
 */
public class DefaultGitManagerTest extends HttpTestCase
{
    @Rule
    public MockitoComponentMockingRule<DefaultGitManager> mocker =
        new MockitoComponentMockingRule<>(DefaultGitManager.class);

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private TestRepository<Repository> remoteRepository;

    private URIish serverURI;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        // Create a repository on the server.
        this.remoteRepository = createTestRepository();

        // Make a first commit since otherwise the clone will fail.
        this.remoteRepository.update(master, this.remoteRepository.commit().create());

        // Add a servlet request handler for our repository and make it require basic authentication.
        ServletContextHandler dBasic = server.authBasic(dumb("/dbasic"));
        this.server.setUp();

        // Save the remote server's URL.
        String srcName = nameOf(this.remoteRepository.getRepository());
        this.serverURI = toURIish(dBasic, srcName);
    }

    private static String nameOf(Repository db) {
        return db.getDirectory().getName();
    }

    @Test
    public void getRepositoryWhenCredentialsProvided() throws Exception
    {
        String repositoryURI = this.serverURI.toASCIIString();
        String localPath = this.tmpFolder.newFolder("getRepositoryWhenCredentialsProvided").toString();
        Repository repository = this.mocker.getComponentUnderTest().getRepository(repositoryURI, localPath,
            AppServer.username, AppServer.password);
        assertNotNull(repository);
    }

    @Test
    public void getRepositoryWhenWrongCredentialsProvided() throws Exception
    {
        String repositoryURI = this.serverURI.toASCIIString();
        String localPath = this.tmpFolder.newFolder("getRepositoryWhenWrongCredentialsProvided").toString();

        try {
            this.mocker.getComponentUnderTest().getRepository(repositoryURI, localPath, "invalidusername",
                "invalidpassword");
            fail("An exception should have been thrown");
        } catch (Exception expected) {
            assertTrue(ExceptionUtils.getRootCauseMessage(expected).matches("TransportException: .*: not authorized"));
        }
    }

    @Test
    public void getRepositoryBareAndCheckBare() throws Exception
    {
        String repositoryURI = this.serverURI.toASCIIString();
        String localPath = this.tmpFolder.newFolder("getRepositoryBareWithCredentials").toString();
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(AppServer.username,
            AppServer.password));
        Repository repository = this.mocker.getComponentUnderTest().getRepositoryBare(repositoryURI, localPath,
            cloneCommand);
        assertNotNull(repository);
        assertEquals(true, repository.isBare());
    }

    /**
     * Register a new servlet request handler to our stub server. Code copied from
     * <a href="https://bit.ly/3cvhRsj">this JGit test</a>.
     */
    private ServletContextHandler dumb(String path)
    {
        File srcGit = this.remoteRepository.getRepository().getDirectory();
        URI base = srcGit.getParentFile().toURI();

        ServletContextHandler ctx = this.server.addContext(path);
        ctx.setResourceBase(base.toString());
        ServletHolder holder = ctx.addServlet(DefaultServlet.class, "/");
        // The tmp directory is symlinked on OS X
        holder.setInitParameter("aliases", "true");
        return ctx;
    }
}
