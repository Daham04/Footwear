package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Product;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadIndexProduct", urlPatterns = {"/LoadIndexProduct"})
public class LoadIndexProduct extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Criteria criteria2 = session.createCriteria(Product.class);
            criteria2.setMaxResults(6);

            List<Product> productList = criteria2.list();

            for (Product product1 : productList) {
                product1.getUser().setEmail(null);
                product1.getUser().setPassword(null);
                product1.getUser().setVerification(null);
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("productList", gson.toJsonTree(productList));

            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
        session.close();

    }

}
