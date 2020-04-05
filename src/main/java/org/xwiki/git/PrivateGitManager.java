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

import org.eclipse.jgit.lib.Repository;
import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Provides services to access a Private Git repository.
 *
 * @version $Id$
 * @since 12.3RC1
 */
@Unstable
@Role
public interface PrivateGitManager
{
    /**
     * Clone a Private Git repository using the credentials provided by user and store it locally in the
     * XWiki Permanent directory. If the repository is already cloned, no action is done.
     *
     * @param repositoryURI the URI to the Git repository to clone (eg "git://github.com/xwiki/xwiki-commons.git")
     * @param localDirectoryName the name of the directory where the Git repository will be cloned (this directory is
     *        relative to the permanent directory
     * @param username the username of Git user
     * @param token the Personal access token generated by the user
     * @return the cloned Repository instance
     */
    Repository getRepository(String repositoryURI, String localDirectoryName, String username, String token);
}
