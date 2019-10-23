/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 */
package org.opengrok.web.api.v1.controller;

import net.sf.cglib.beans.BeanGenerator;
import org.modelmapper.ModelMapper;
import org.opengrok.indexer.configuration.RuntimeEnvironment;
import org.opengrok.indexer.history.RepositoryInfo;
import org.opengrok.indexer.util.ClassUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Field;

@Path("/repositories")
public class RepositoriesController {

    private RuntimeEnvironment env = RuntimeEnvironment.getInstance();

    static class RepositoryInfoDTO {
        // Contains all members of RepositoryInfo except datePatterns
        String directoryNameRelative;
        Boolean working;
        String type;
        boolean remote;
        String parent;
        String branch;
        String currentVersion;
    }

    private Object createRepositoryInfoTO(RepositoryInfo ri) {
        // ModelMapper assumes getters/setters so use BeanGenerator to provide them.
        BeanGenerator beanGenerator = new BeanGenerator();
        for (Field field : RepositoryInfoDTO.class.getDeclaredFields()) {
            beanGenerator.addProperty(field.getName(), field.getType());
        }
        Object bean = beanGenerator.create();

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(ri, bean.getClass());
    }

    private Object getRepositoryInfoData(String repositoryPath) {
        for (RepositoryInfo ri : env.getRepositories()) {
            if (ri.getDirectoryNameRelative().equals(repositoryPath)) {
                return createRepositoryInfoTO(ri);
            }
        }

        return null;
    }

    @GET
    @Path("/property/{field}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object get(@QueryParam("repository") final String repositoryPath, @PathParam("field") final String field)
            throws IOException {

        Object ri = getRepositoryInfoData(repositoryPath);
        if (ri == null) {
            throw new WebApplicationException("cannot find repository with path: " + repositoryPath,
                    Response.Status.NOT_FOUND);
        }

        return ClassUtil.getFieldValue(ri, field);
    }
}
