package com.book.buy.servlet;

import com.book.buy.dao.BookDao;
import com.book.buy.dao.ComplainDao;
import com.book.buy.dao.InformDao;
import com.book.buy.dao.UserDao;
import com.book.buy.factory.BookDaoImpFactory;
import com.book.buy.factory.ComplainDaoImpFactory;
import com.book.buy.factory.InformDaoImplFactory;
import com.book.buy.factory.UserDaoImpFactory;
import com.book.buy.utils.NewDate;
import com.book.buy.vo.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by violet on 2015/11/26.
 */
@WebServlet(name = "UpBookByAdminServlet", urlPatterns = "/upbookbyadmin")
public class UpBookByAdminServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");

        PrintWriter out = response.getWriter();
        String href = "";
        int compnum = 0;

        //校验管理员的登陆状态
        ManagerVo admin = (ManagerVo)request.getSession().getAttribute("admin");
        if (null == admin){
            href = "/loginmanager";
            out.print("<script language='javascript'>alert('登录状态失效，管理员请登陆！');"
                    + "window.location.href='" + href + "';</script>");
            return;
        }

        //拿到jsp页面传来的bookid、appealid的值，并将其转换为int型的
        String bookid = request.getParameter("bookid");
        String appealid = request.getParameter("appealid");

        int bid = Integer.parseInt(bookid);
        int aid = Integer.parseInt(appealid);

        BookDao bookdao = BookDaoImpFactory.getBookDaoImpl();
        UserDao userdao = UserDaoImpFactory.getUserDaoImpl();
        InformDao informdao = InformDaoImplFactory.getInformDaoImpl();
        ComplainDao appealdao = ComplainDaoImpFactory.getCompDaoImp();

        BookVo bookvo = new BookVo();
        UserVo uservo = new UserVo();
        InformVo informvo = new InformVo();
        ComplainVo appealvo = new ComplainVo();

        //根据bid和aid查找到响应的书籍和申诉信息，并对其状态字段进行修改，并update到数据库中
        try {
            bookvo = bookdao.findById(bid);
            appealvo = appealdao.getCompById(aid);

            uservo = userdao.findUserById(bookvo.getUserID());

            compnum = uservo.getComplainNum();
            compnum = compnum-1;                 //被投诉次数减一
            uservo.setComplainNum(compnum);

            userdao.updateUser(uservo);
            bookvo.setState(1);
            appealvo.setState(1);

            bookdao.updateBook(bookvo);
            appealdao.updateComp(appealvo);

            //把申诉处理消息放入inform表中
            informvo.setUserID(bookvo.getUserID());
            informvo.setType(4);
            informvo.setNum(appealvo.getId());
            Date date = new Date();
            String time = NewDate.getDateTime(date);
            informvo.setTime(time);
            informdao.addInform(informvo);

            //判断用户是否可以解除冻结，若可以则给用户发个消息
            if (compnum == 2){
                informvo.setUserID(bookvo.getUserID());
                informvo.setType(6);
                informvo.setNum(0);
                Date date1 = new Date();
                String time1 = NewDate.getDateTime(date1);
                informvo.setTime(time1);
                informdao.addInform(informvo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        try {
            bookdao.close();
            userdao.close();
            appealdao.close();
            informdao.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        href = "/getappealdetil?appid="+aid;
        out.print("<script language='javascript'>alert('上架成功');"
                + "window.location.href='" + href + "';</script>");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
