/**
 * <a href=“http://www.openolat.org“>
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * 2011 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.modules.fo.restapi;

import static org.olat.collaboration.CollaborationTools.KEY_FORUM;
import static org.olat.collaboration.CollaborationTools.PROP_CAT_BG_COLLABTOOLS;
import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.security.RestSecurityHelper.isAdmin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.run.navigation.NavigationHandler;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.SearchBusinessGroupParams;
import org.olat.modules.fo.Forum;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.restapi.group.LearningGroupWebService;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Path("users/{identityKey}/forums")
public class MyForumsWebService {
	
	private static final OLog log = Tracing.createLoggerFor(MyForumsWebService.class);

	
	/**
	 * Retrieves the forum of a group
	 * @response.representation.200.qname {http://www.example.com}forumVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The forum
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param groupKey The key of the group
	 * @param courseNodeId The key of the node if it's a course
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The files
	 */
	@Path("group/{groupKey}")
	public ForumWebService getGroupForum(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(groupKey == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		}
		return new LearningGroupWebService().getForum(groupKey, request);
	}
	
	/**
	 * Retrieves the folrum of a course building block
	 * @response.representation.200.qname {http://www.example.com}fileVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The files
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param courseKey The key of the course
	 * @param courseNodeId The key of the node
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The files
	 */
	@Path("course/{courseKey}/{courseNodeId}")
	public ForumWebService getCourseFolder(@PathParam("courseKey") Long courseKey, @PathParam("courseNodeId") String courseNodeId,
			@Context HttpServletRequest request) {
		
		if(courseNodeId == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		} else if (courseKey != null) {
			ICourse course = loadCourse(courseKey);
			if(course == null) {
				throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
			}
			CourseNode node =course.getEditorTreeModel().getCourseNode(courseNodeId);
			if(node == null) {
				throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
			} else if(!(node instanceof FOCourseNode)) {
				throw new WebApplicationException(Response.serverError().status(Status.NOT_ACCEPTABLE).build());
			}
			
			FOCourseNode bcNode = (FOCourseNode)node;
			UserRequest ureq = getUserRequest(request);
			UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course.getCourseEnvironment());
			NodeEvaluation ne = bcNode.eval(uce.getConditionInterpreter(), new TreeEvaluation());

			boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(ne);
			if(mayAccessWholeTreeUp) {
				Forum forum = bcNode.loadOrCreateForum(course.getCourseEnvironment());
				return new ForumWebService(forum);
			} else {
				throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
			}
		}
		return null;
	}
	
	/**
	 * Retrieves a list of forums on a user base. All forums of groups 
	 * where the user is participant/tutor + all forums in course where
	 * the user is a participant (owner, tutor or participant)
	 * @response.representation.200.qname {http://www.example.com}forumVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The forums
	 * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_FORUMVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param identityKey The key of the user (IdentityImpl)
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The forums
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getForums(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		
		Roles roles;
		Identity retrievedUser = getIdentity(httpRequest);
		if(retrievedUser == null) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		} else if(!identityKey.equals(retrievedUser.getKey())) {
			if(isAdmin(httpRequest)) {
				retrievedUser = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey);
				roles = BaseSecurityManager.getInstance().getRoles(retrievedUser);
			} else {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		} else {
			roles = getRoles(httpRequest);
		}

		Map<Long,Long> groupNotified = new HashMap<Long,Long>();
		Map<Long,Long> courseNotified = new HashMap<Long,Long>();
		NotificationsManager man = NotificationsManager.getInstance();
		{//collect subscriptions
			List<String> notiTypes = Collections.singletonList("Forum");
			List<Subscriber> subs = man.getSubscribers(retrievedUser, notiTypes);
			for(Subscriber sub:subs) {
				String resName = sub.getPublisher().getResName();
				Long forumKey = Long.parseLong(sub.getPublisher().getData());
				if("BusinessGroup".equals(resName)) {
					Long groupKey = sub.getPublisher().getResId();
					groupNotified.put(groupKey, forumKey);
				} else if("CourseModule".equals(resName)) {
					Long courseKey = sub.getPublisher().getResId();
					courseNotified.put(courseKey, forumKey);
				}
			}
		}
		

		List<ForumVO> forumVOs = new ArrayList<ForumVO>();
		
		RepositoryManager rm = RepositoryManager.getInstance();
		ACFrontendManager acManager = (ACFrontendManager)CoreSpringFactory.getBean("acFrontendManager");
		SearchRepositoryEntryParameters repoParams = new SearchRepositoryEntryParameters(retrievedUser, roles, "CourseModule");
		repoParams.setOnlyExplicitMember(true);
		List<RepositoryEntry> entries = rm.genericANDQueryWithRolesRestriction(repoParams, 0, -1, true);
		for(RepositoryEntry entry:entries) {
			AccessResult result = acManager.isAccessible(entry, retrievedUser, false);
			if(result.isAccessible()) {
				ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
				CourseNode rootNode = course.getRunStructure().getRootNode();
				
				//collect the forums
				final List<FOCourseNode> forumsNode = new ArrayList<FOCourseNode>();
				TreeVisitor treeVisitor = new TreeVisitor(new Visitor() {
					@Override
					public void visit(INode node) {
						if(node instanceof FOCourseNode) {
							forumsNode.add((FOCourseNode)node);
						}
					}
				}, rootNode, false);
				treeVisitor.visitAll();
				
				//check the access
				if(!forumsNode.isEmpty()) {
					IdentityEnvironment ienv = new IdentityEnvironment();
					ienv.setIdentity(retrievedUser);
					ienv.setRoles(roles);
					UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());

					TreeEvaluation treeEval = new TreeEvaluation();
					rootNode.eval(userCourseEnv.getConditionInterpreter(), treeEval);
					
					for(FOCourseNode forumNode:forumsNode) {
						TreeNode newCalledTreeNode = treeEval.getCorrespondingTreeNode(forumNode);
						if (newCalledTreeNode != null) {
							NodeEvaluation nodeEval = (NodeEvaluation) newCalledTreeNode.getUserObject();
							boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(nodeEval);
							if(mayAccessWholeTreeUp) {
								Forum forum = forumNode.loadOrCreateForum(course.getCourseEnvironment());
								
								ForumVO forumVo = new ForumVO(forum);
								forumVo.setName(course.getCourseTitle());
								forumVo.setDetailsName(forumNode.getShortTitle());
								forumVo.setSubscribed(courseNotified.containsKey(entry.getKey()));
								forumVo.setForumKey(courseNotified.get(entry.getKey()));
								forumVo.setCourseKey(course.getResourceableId());
								forumVo.setCourseNodeId(forumNode.getIdent());
								forumVOs.add(forumVo);
							}
						}
					}
				}
			}
		}
		
		//start found forums in groups
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_BUDDYGROUP, BusinessGroup.TYPE_LEARNINGROUP);
		params.addTools(CollaborationTools.TOOL_FORUM);
		List<BusinessGroup> groups = bgm.findBusinessGroups(params, retrievedUser, true, true, null, 0, -1);
		//list forum keys
		List<Long> groupIds = new ArrayList<Long>();
		Map<Long,BusinessGroup> groupsMap = new HashMap<Long,BusinessGroup>();
		for(BusinessGroup group:groups) {
			if(groupNotified.containsKey(group.getKey())) {
				ForumVO forumVo = new ForumVO();
				forumVo.setName(group.getName());
				forumVo.setGroupKey(group.getKey());
				forumVo.setForumKey(groupNotified.get(group.getKey()));
				forumVo.setSubscribed(true);
				forumVOs.add(forumVo);
				
				groupIds.remove(group.getKey());
			} else {
				groupIds.add(group.getKey());
				groupsMap.put(group.getKey(), group);
			}
		}

		PropertyManager pm = PropertyManager.getInstance();
		List<Property> forumProperties = pm.findProperties(OresHelper.calculateTypeName(BusinessGroup.class), groupIds, PROP_CAT_BG_COLLABTOOLS, KEY_FORUM);
		for(Property prop:forumProperties) {
			Long forumKey = prop.getLongValue();
			if(forumKey != null && groupsMap.containsKey(prop.getResourceTypeId())) {
				BusinessGroup group = groupsMap.get(prop.getResourceTypeId());
				
				ForumVO forumVo = new ForumVO();
				forumVo.setName(group.getName());
				forumVo.setGroupKey(group.getKey());
				forumVo.setForumKey(prop.getLongValue());
				forumVo.setSubscribed(false);
				forumVOs.add(forumVo);
			}
		}

		ForumVOes voes = new ForumVOes();
		voes.setForums(forumVOs.toArray(new ForumVO[forumVOs.size()]));
		voes.setTotalCount(forumVOs.size());
		return Response.ok(voes).build();
	}
	
	private ICourse loadCourse(Long courseId) {
		try {
			ICourse course = CourseFactory.loadCourse(courseId);
			return course;
		} catch(Exception ex) {
			log.error("cannot load course with id: " + courseId, ex);
			return null;
		}
	}
}