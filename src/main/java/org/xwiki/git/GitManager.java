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
package org.xwiki.git;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.gitective.core.stat.UserCommitActivity;
import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Provides services to access a Git repository.
 *
 * @version $Id$
 * @since 5.3M2
 */
@Role
public interface GitManager
{
    /**
     * Clone a Git repository by storing it locally in the XWiki Permanent directory. If the repository is already
     * cloned, no action is done.
     *
     * @param repositoryURI the URI to the Git repository to clone (eg "git://github.com/xwiki/xwiki-commons.git")
     * @param localDirectoryName the name of the directory where the Git repository will be cloned (this directory is
     *        relative to the permanent directory
     * @return the cloned Repository instance
     */
    Repository getRepository(String repositoryURI, String localDirectoryName);

    /**
     * Clone a Private Git repository using the credentials provided by user and store it locally in the
     * XWiki Permanent directory. If the repository is already cloned, no action is done.
     *
     * @param repositoryURI the URI to the Git repository to clone (eg "git://github.com/xwiki/xwiki-commons.git")
     * @param localDirectoryName the name of the directory where the Git repository will be cloned (this directory is
     *        relative to the permanent directory
     * @param username the username of the Git user
     * @param accessCode the password or OAuth or personal access token that authenticates with the Git user
     * @return the cloned Repository instance
     * @since 9.9
     */
    @Unstable
    default Repository getRepository(String repositoryURI, String localDirectoryName, String username,
        String accessCode)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Clone a Git repository as Bare using the CloneCommand provided by user and store it locally in the
     * XWiki Permanent directory. If the repository is already cloned, no action is done.
     *
     * @param repositoryURI the URI to the Git repository to clone (eg "git://github.com/xwiki/xwiki-commons.git")
     * @param localDirectoryName the name of the directory where the Git repository will be cloned (this directory is
     *        relative to the permanent directory
     * @param cloneCommand the clone settings used as CloneCommand object
     * @return the cloned Repository instance
     * @since 9.10
     */
    @Unstable
    default Repository getRepositoryBare(String repositoryURI, String localDirectoryName, CloneCommand cloneCommand)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Find all authors who have ever committed code in the passed repositories.
     *
     * @param repositories the list of repositories in which to look for authors
     * @return the list of authors who have ever contributed code in the passed repository
     */
    Set<PersonIdent> findAuthors(List<Repository> repositories);

    /**
     * Count commits done by all authors in the passed repositories and since the passed date. Note that authors are
     * uniquely identified by their email addresses.
     *
     * @param since the date from which to start counting. If null then counts from the beginning
     * @param repositories the list of repositories in which to look for commits
     * @return the author commit activity
     */
    UserCommitActivity[] countAuthorCommits(Date since, List<Repository> repositories);
}
