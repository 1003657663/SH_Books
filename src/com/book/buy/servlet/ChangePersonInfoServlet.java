package com.book.buy.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.book.buy.dao.LocationDao;
import com.book.buy.dao.UserDao;
import com.book.buy.factory.LocationDaoImpFactory;
import com.book.buy.factory.UserDaoImpFactory;
import com.book.buy.vo.LocationVo;
import com.book.buy.vo.UserVo;

/**
 * 修改用户信息的servlet
 * Servlet implementation class ChangePersonInfoServlet
 */
@WebServlet("/ChangePersonInfoServlet")
public class ChangePersonInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
        public ChangePersonInfoServlet() {
            super();
        }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
	    String newPath = null;
	    String extName = null;
	    String newName = null;
	    Integer mark   = 0;
	    PrintWriter out = response.getWriter();
	    //获取表单数据
	    DiskFileItemFactory factory = new DiskFileItemFactory();
		//创建文件上传工具
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
		    	//获取所有的列表项
			List<FileItem> list = upload.parseRequest(request);
			
			for(int i = 0; i < list.size(); ++i){
				FileItem item = list.get(i);
				//非文件的其他项
				if(item.isFormField()){
				    String name = item.getFieldName();
				    String value = new String(item.getString().getBytes("ISO-8859-1"), "utf-8");
				    request.setAttribute(name, value);
				}else{
				    //上传图片
				    //文件项
					if(item.getSize() != 0) {
					    	mark = 1;
						String filename = item.getName();
						extName = filename.substring(filename.lastIndexOf("."));
						newName = UUID.randomUUID().toString();
						String rootPath = request.getServletContext().getRealPath("/images");
						newPath = rootPath + "/" + newName + extName;
						item.write(new File(newPath));
						request.getSession().setAttribute("newImagePath", newPath);
					}
				}
			}
			
		} 
		catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    //获取参数
	    newPath = "/images/" + newName + extName;
	    String mima = (String) request.getAttribute("mima");
	    String username = (String) request.getAttribute("username");
	    String phoneNumber = (String) request.getAttribute("tel");
	    String qq = (String) request.getAttribute("qq");
	    String dorName = (String) request.getAttribute("yuanqu");
	    String dorNum = (String) request.getAttribute("jihaolou");
	    String floorNum = (String) request.getAttribute("jilingji");
	    
	    String href = "/SH_Books";
	    //判断输入的条件是否满足
	    //密码空
	    if(mima == null){
		href += "/changePersonInfo";
		out.print("<script language='javascript'>alert('修改时密码不能为空！！！');"
		    	+ "window.location.href='"+ href + "';</script>");
		return;
	    }
	    //收货地址
	    if(dorName == null || dorNum == null 
		    || dorName.equals("") || dorNum.equals("")){
		href += "/changePersonInfo";
		out.print("<script language='javascript'>alert('请填写收货地址！！！');"
		    	+ "window.location.href='"+ href + "';</script>");
		return;
	    }
	    //电话11位
	    if(phoneNumber.length() != 11){
		href += "/changePersonInfo";
		out.print("<script language='javascript'>alert('电话格式错误！！！');"
		    	+ "window.location.href='"+ href + "';</script>");
		return;
	    }
	    else{
		//全数字
		for(int i = 0; i < 11; ++i){
		    if(!Character.isDigit(phoneNumber.charAt(i))){
			href += "/changePersonInfo";
			out.print("<script language='javascript'>alert('电话格式错误！！！');"
			    	+ "window.location.href='"+ href + "';</script>");
			return;
		    }
		}
	    }
	    //qq格式
	    for(int i = 0; i < qq.length(); ++i){
		if(!Character.isDigit(qq.charAt(i))){
		    href += "/changePersonInfo";
		    out.print("<script language='javascript'>alert('qq格式错误！！！');"
			+ "window.location.href='"+ href + "';</script>");
		    return;
		}
	    }
	    //获取用户信息
	    UserVo userVo = (UserVo) request.getSession().getAttribute("user");
	    userVo.setUsername(username);
	    userVo.setPassword(mima);
	    userVo.setPhoneNumber(phoneNumber);
	    userVo.setQq(qq);
	    if(mark == 1)
		userVo.setHeadPhoto(newPath);
	    //获取用户地址修改或增加
	    LocationVo locationVo = (LocationVo) request.getSession().getAttribute("location");
	    LocationDao locationDao = LocationDaoImpFactory.getLocationDaoImp();
	    //更新数据并加入session
	    UserDao userDao = null;
	    try {
		//获取用户Dao
		userDao = UserDaoImpFactory.getUserDaoImpl();
		//更新用户信息
		userDao.updateUser(userVo);
		request.getSession().setAttribute("user", userVo);
		//更新住址信息
		if(locationVo == null){
		    locationVo  = new LocationVo(dorName, Integer.parseInt(dorNum), Integer.parseInt(floorNum));
		    locationVo.setUserID(userVo.getId());
		    locationDao.addLocation(locationVo);
		}else{
		    locationVo.setDorName(dorName);
		    locationVo.setDorNum(Integer.parseInt(dorNum));
		    locationVo.setFloorNum(Integer.parseInt(floorNum));
		    locationDao.UpdateLocation(locationVo);
		}
		request.getSession().setAttribute("location", locationVo);
		
	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    //关闭流
	    locationDao.close();
	    userDao.close();
	    //跳转
	    response.sendRedirect("/personInfo");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		doGet(request, response);
	}

}
