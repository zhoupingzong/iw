package com.xnx3.j2ee.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.xnx3.Lang;
import com.xnx3.StringUtil;
import com.xnx3.j2ee.Global;
import com.xnx3.j2ee.dao.LogDAO;
import com.xnx3.j2ee.dao.PostClassDAO;
import com.xnx3.j2ee.dao.PostCommentDAO;
import com.xnx3.j2ee.dao.PostDAO;
import com.xnx3.j2ee.dao.PostDataDAO;
import com.xnx3.j2ee.dao.UserDAO;
import com.xnx3.j2ee.entity.BaseEntity;
import com.xnx3.j2ee.entity.Post;
import com.xnx3.j2ee.entity.PostClass;
import com.xnx3.j2ee.entity.PostData;
import com.xnx3.j2ee.entity.User;
import com.xnx3.j2ee.service.PostService;
import com.xnx3.j2ee.shiro.ShiroFunc;
import com.xnx3.j2ee.vo.BaseVO;
import com.xnx3.j2ee.vo.PostVO;

@Service("postService")
public class PostServiceImpl implements PostService {
	
	@Resource
	private PostDAO postDAO;
	@Resource
	private PostDataDAO postDataDAO;
	@Resource
	private LogDAO logDAO;
	@Resource
	private UserDAO userDAO;
	@Resource
	private PostClassDAO postClassDAO;
	@Resource
	private PostCommentDAO postCommentDAO;
	
	@Override
	public void save(Post transientInstance) {
		// TODO Auto-generated method stub
		postDAO.save(transientInstance);
	}

	@Override
	public void delete(Post persistentInstance) {
		// TODO Auto-generated method stub
		postDAO.delete(persistentInstance);
	}

	@Override
	public Post findById(Integer id) {
		// TODO Auto-generated method stub
		return postDAO.findById(id);
	}

	@Override
	public List<Post> findByExample(Post instance) {
		// TODO Auto-generated method stub
		return postDAO.findByExample(instance);
	}

	@Override
	public List findByProperty(String propertyName, Object value) {
		// TODO Auto-generated method stub
		return postDAO.findByProperty(propertyName, value);
	}

	@Override
	public List<Post> findByClassid(Object classid) {
		// TODO Auto-generated method stub
		return postDAO.findByClassid(classid);
	}

	@Override
	public List<Post> findByTitle(Object title) {
		// TODO Auto-generated method stub
		return postDAO.findByTitle(title);
	}

	@Override
	public List<Post> findByView(Object view) {
		// TODO Auto-generated method stub
		return postDAO.findByView(view);
	}

	@Override
	public List<Post> findByInfo(Object info) {
		// TODO Auto-generated method stub
		return postDAO.findByInfo(info);
	}

	@Override
	public List<Post> findByAddtime(Object addtime) {
		// TODO Auto-generated method stub
		return postDAO.findByAddtime(addtime);
	}

	public List<Post> findByState(Object state){
		return postDAO.findByState(state);
	}
	
	@Override
	public List<Post> findByUserid(Object userid) {
		// TODO Auto-generated method stub
		return postDAO.findByUserid(userid);
	}

	@Override
	public List findAll() {
		// TODO Auto-generated method stub
		return postDAO.findAll();
	}

	@Override
	public Post merge(Post detachedInstance) {
		// TODO Auto-generated method stub
		return postDAO.merge(detachedInstance);
	}

	@Override
	public void attachDirty(Post instance) {
		// TODO Auto-generated method stub
		postDAO.attachDirty(instance);
	}

	@Override
	public void attachClean(Post instance) {
		// TODO Auto-generated method stub
		postDAO.attachClean(instance);
	}

	@Override
	public BaseVO savePost(HttpServletRequest request) {
		BaseVO baseVO = new BaseVO();
		int id = Lang.stringToInt(request.getParameter("id"), 0);
		int classid = Lang.stringToInt(request.getParameter("classid"), 0);
		String title = request.getParameter("title");
		String text = request.getParameter("text");
		
		if(classid == 0){
			baseVO.setBaseVO(BaseVO.FAILURE, "请选择发布的板块");
			return baseVO;
		}
		if(title==null || title.length()<Global.bbs_titleMinLength || title.length()>Global.bbs_titleMaxLength){
			baseVO.setBaseVO(BaseVO.FAILURE, "标题必须是"+Global.bbs_titleMinLength+"到"+Global.bbs_titleMaxLength+"个字母或汉字");
			return baseVO;
		}
		if(text==null || text.length()<Global.bbs_textMinLength){
			baseVO.setBaseVO(BaseVO.FAILURE, "内容不能少于"+Global.bbs_textMinLength+"个字母或汉字");
			return baseVO;
		}
		
		Post post = new com.xnx3.j2ee.entity.Post();
		PostData postData = new PostData();
		if(id != 0){
			post = findById(id);
			if(post == null){
				baseVO.setBaseVO(BaseVO.FAILURE, "要修改的帖子不存在！");
				return baseVO;
			}else{
				post.setId(id);
				postData = postDataDAO.findById(post.getId());
			}
		}else{
			post.setAddtime(com.xnx3.DateUtil.timeForUnix10());
			post.setState(Post.STATE_NORMAL);
			post.setUserid(ShiroFunc.getUser().getId());
			post.setView(0);
			post.setIsdelete(Post.ISDELETE_NORMAL);
		}
		
		String info="";	//截取简介文字,30字
		String filterText = StringUtil.filterHtmlTag(text);
		if(filterText.length()<60){
			info=filterText;
		}else{
			info=filterText.substring(0,60);
		}
		
		post.setTitle(title);
		post.setClassid(classid);
		post.setInfo(info);
		save(post);
		
		if(postData.getPostid()==null){
			postData.setPostid(post.getId());
		}
		postData.setText(text);
		postDataDAO.save(postData);
		
		baseVO.setBaseVO(BaseVO.SUCCESS, post.getId()+"");
		if(id == 0){
			logDAO.insert(post.getId(), "BBS_POST_ADD", post.getTitle());
		}else{
			logDAO.insert(post.getId(), "BBS_POST_UPDATE", post.getTitle());
		}
		
		return baseVO;
	}

	@Override
	public BaseVO deletePost(int id) {
		BaseVO baseVO = new BaseVO();
		if(id>0){
			Post p = findById(id);
			if(p!=null){
				p.setIsdelete(BaseEntity.ISDELETE_DELETE);
				save(p);
				logDAO.insert(p.getId(), "ADMIN_SYSTEM_BBS_POST_DELETE", p.getTitle());
			}else{
				baseVO.setBaseVO(BaseVO.FAILURE, "要删除的帖子不存在！");
			}
		}else{
			baseVO.setBaseVO(BaseVO.FAILURE, "请传入要删除的帖子编号");
		}
		return baseVO;
	}

	@Override
	public PostVO read(int id) {
		PostVO postVO = new PostVO();
		
		if(id>0){
			//查询帖子详情
			Post post=findById(id);
			if(post == null){
				postVO.setBaseVO(PostVO.FAILURE, "您所查看的帖子不存在");
				return postVO;
			}
			
			//查看帖子所属用户
			User user = userDAO.findById(post.getUserid());
			//检验此用户状态是否正常，是否被冻结
			if(user.getIsfreeze() == User.ISFREEZE_FREEZE){
				postVO.setBaseVO(BaseVO.FAILURE, "发帖者账号已被冻结！无法查看帖子");
				return postVO;
			}
			postVO.setUser(user);
			
			//查所属板块
			PostClass postClass = postClassDAO.findById(post.getClassid());
			if(postClass == null || postClass.getIsdelete() == BaseEntity.ISDELETE_DELETE){
				postVO.setBaseVO(PostVO.FAILURE, "您所查看的帖子所属板块不存在或已被删除！");
			}else{
				postVO.setPostClass(postClass);
				postVO.setPost(post);
				post.setView(post.getView()+1);
				save(post);
				
				PostData postData = postDataDAO.findById(post.getId());
				postVO.setText(postData.getText());
				
				postVO.setCommentCount(postCommentDAO.count(post.getId()));
				
				if(Global.bbs_readPost_addLog){
					logDAO.insert(post.getId(), "BBS_POST_VIEW", post.getTitle());
				}
			}
		}else{
			postVO.setBaseVO(PostVO.FAILURE, "请传入帖子id");
		}
		
		return postVO;
	}

}