package com.book.buy.servlet;

import com.book.buy.dao.FeedBackDao;
import com.book.buy.dao.UserDao;
import com.book.buy.factory.FeedBackDaoImplFactory;
import com.book.buy.factory.UserDaoImpFactory;
import com.book.buy.vo.FeedBackVo;
import com.book.buy.vo.ManagerVo;
import com.book.buy.vo.UserVo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Created by violet on 2015/12/7.
 */
@WebServlet(name = "GetFedbackDetilServlet", urlPatterns = "/getfedbackdetil")
public class GetFedbackDetilServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");

        PrintWriter out = response.getWriter();
        String href = "";

        //管理员登陆信息校验
        ManagerVo admin = (ManagerVo)request.getSession().getAttribute("admin");
        if (null == admin){
            href = "/loginmanager";
            out.print("<script language='javascript'>alert('登录状态失效，管理员请登陆！');"
                    + "window.location.href='" + href + "';</script>");
            return;
        }

        //获得jsp页面传来的参数
        String userid = (String)request.getParameter("userid");
        String time = (String)request.getParameter("time");
        int id = Integer.parseInt(userid);

        FeedBackVo fed = new FeedBackVo();
        UserVo user = new UserVo();
        FeedBackDao feddao = FeedBackDaoImplFactory.getFeedBackDaoImpl();
        UserDao userdao = UserDaoImpFactory.getUserDaoImpl();

        //在数据库中根据id和time来查找具体的反馈
        try {
            fed = feddao.findbyut(id, time);
            user = userdao.findUserById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //把查找到的结果放在session中
        request.getSession().setAttribute("feddetil", fed);
        request.getSession().setAttribute("feduser", user);

        feddao.close();
        userdao.close();

        response.sendRedirect("/fedbackdetil");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
