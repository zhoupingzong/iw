package com.xnx3.j2ee.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.xnx3.DateUtil;
import com.xnx3.Lang;
import com.xnx3.StringUtil;
import com.xnx3.j2ee.Global;
import com.xnx3.j2ee.dao.LogDAO;
import com.xnx3.j2ee.dao.PostCommentDAO;
import com.xnx3.j2ee.dao.PostDAO;
import com.xnx3.j2ee.entity.BaseEntity;
import com.xnx3.j2ee.entity.Post;
import com.xnx3.j2ee.entity.PostClass;
import com.xnx3.j2ee.entity.PostComment;
import com.xnx3.j2ee.generateCache.Bbs;
import com.xnx3.j2ee.service.PostCommentService;
import com.xnx3.j2ee.shiro.ShiroFunc;
import com.xnx3.j2ee.vo.BaseVO;

@Service("postCommentService")
public class PostCommentServiceImpl implements PostCommentService {

	@Resource
	private PostCommentDAO postCommentDAO;
	@Resource
	private LogDAO logDAO;
	@Resource
	private PostDAO postDAO;
	
	@Override
	public void save(PostComment transientInstance) {
		// TODO Auto-generated method stub
		postCommentDAO.save(transientInstance);
	}

	@Override
	public void delete(PostComment persistentInstance) {
		// TODO Auto-generated method stub
		postCommentDAO.delete(persistentInstance);
	}

	@Override
	public PostComment findById(Integer id) {
		// TODO Auto-generated method stub
		return postCommentDAO.findById(id);
	}

	@Override
	public List<PostComment> findByExample(PostComment instance) {
		// TODO Auto-generated method stub
		return postCommentDAO.findByExample(instance);
	}

	@Override
	public List findByProperty(String propertyName, Object value) {
		// TODO Auto-generated method stub
		return postCommentDAO.findByProperty(propertyName, value);
	}

	@Override
	public List<PostComment> findByPostid(Object postid) {
		// TODO Auto-generated method stub
		return postCommentDAO.findByPostid(postid);
	}

	@Override
	public List<PostComment> findByAddtime(Object addtime) {
		// TODO Auto-generated method stub
		return postCommentDAO.findByAddtime(addtime);
	}

	@Override
	public List<PostComment> findByUserid(Object userid) {
		// TODO Auto-generated method stub
		return postCommentDAO.findByUserid(userid);
	}

	@Override
	public List<PostComment> findByText(Object text) {
		// TODO Auto-generated method stub
		return postCommentDAO.findByText(text);
	}

	@Override
	public List findAll() {
		// TODO Auto-generated method stub
		return postCommentDAO.findAll();
	}

	@Override
	public PostComment merge(PostComment detachedInstance) {
		// TODO Auto-generated method stub
		return postCommentDAO.merge(detachedInstance);
	}

	@Override
	public void attachDirty(PostComment instance) {
		// TODO Auto-generated method stub
		postCommentDAO.attachDirty(instance);
	}

	@Override
	public void attachClean(PostComment instance) {
		// TODO Auto-generated method stub
		postCommentDAO.attachClean(instance);
	}

	@Override
	public List commentAndUser(int postid) {
		// TODO Auto-generated method stub
		return postCommentDAO.commentAndUser(postid);
	}

	@Override
	public List commentAndUser(int postid, int limit) {
		// TODO Auto-generated method stub
		return postCommentDAO.commentAndUser(postid, limit);
	}

	@Override
	public int count(int postid) {
		return postCommentDAO.count(postid);
	}

	@Override
	public BaseVO deleteComment(int id) {
		BaseVO baseVO = new BaseVO();
		if(id>0){
			PostComment pc = findById(id);
			if(pc!=null){
				pc.setIsdelete(BaseEntity.ISDELETE_DELETE);
				save(pc);
				logDAO.insert(pc.getId(), "BBS_POST_DELETE_COMMENT", pc.getText());
			}else{
				baseVO.setBaseVO(BaseVO.FAILURE, "要删除的评论不存在！");
			}
		}else{
			baseVO.setBaseVO(BaseVO.FAILURE, "请传入正确的评论编号");
		}
		return baseVO;
	}

	@Override
	public BaseVO addComment(HttpServletRequest request) {
		BaseVO baseVO = new BaseVO();
		int postid = Lang.stringToInt(request.getParameter("postid"), 0);
		String text = request.getParameter("text");
		if(postid == 0){
			baseVO.setBaseVO(BaseVO.FAILURE, "要回复哪篇呢？");
			return baseVO;
		}
		
		if(text==null || text.length()<Global.bbs_commentTextMinLength || text.length()>Global.bbs_commentTextMaxLength){
			baseVO.setBaseVO(BaseVO.FAILURE, "回复内容长度必须在"+Global.bbs_commentTextMinLength+"到"+Global.bbs_commentTextMaxLength+"个英文或汉字之间");
			return baseVO;
		}
		
		//先查询是不是有这个主贴
		Post p=postDAO.findById(postid);
		if(p == null){
			baseVO.setBaseVO(BaseVO.FAILURE, "要回复的帖子不存在！");
			return baseVO;
		}
		
		if(p.getState() != Post.STATE_NORMAL){
			baseVO.setBaseVO(BaseVO.FAILURE, "要回复的帖子非正常状态，无法回复");
			return baseVO;
		}
		
		if(p.getIsdelete() == Post.ISDELETE_DELETE){
			baseVO.setBaseVO(BaseVO.FAILURE, "要回复的帖子已删除，无法回复");
			return baseVO;
		}
		
		PostComment postComment=new PostComment();
		postComment.setPostid(p.getId());
		postComment.setUserid(ShiroFunc.getUser().getId());
		postComment.setAddtime(DateUtil.timeForUnix10());
		postComment.setText(text);
		postComment.setIsdelete(PostComment.ISDELETE_NORMAL);
		save(postComment);
		
		logDAO.insert(postComment.getId(), "BBS_POST_COMMENT_ADD", StringUtil.filterHtmlTag(postComment.getText()));
		return baseVO;
	}
}