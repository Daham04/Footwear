package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Category;
import entity.Color;
import entity.Product;
import entity.Size;
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
import org.hibernate.criterion.Order;

@WebServlet(name = "SearchProducts", urlPatterns = {"/SearchProducts"})
public class SearchProducts extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("success", false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        //Main code
        
        //get category list
        Criteria criteria1 = session.createCriteria(Category.class);
        List<Category> categoryList = criteria1.list();
        jsonObject.add("categoryList", gson.toJsonTree(categoryList));
        
        //get color list
        Criteria criteria3 = session.createCriteria(Color.class);
        List<Color> colorList = criteria3.list();
        jsonObject.add("colorList", gson.toJsonTree(colorList));
        
        //get storage list
        Criteria criteria4 = session.createCriteria(Size.class);
        List<Size> sizeList = criteria4.list();
        jsonObject.add("sizeList", gson.toJsonTree(sizeList));
        
        //get product list
        Criteria criteria5 = session.createCriteria(Product.class);
        
        //get lateset product
        criteria5.addOrder(Order.desc("id"));
        jsonObject.addProperty("allProductCount", criteria5.list().size());
        
        //get product range
        criteria5.setFirstResult(0);
        criteria5.setMaxResults(5);
        
        List<Product> productList = criteria5.list();
        
        //remove user from the product
        for (Product product : productList) {
              product.setUser(null);
        }
        jsonObject.add("productList", gson.toJsonTree(productList));
        jsonObject.addProperty("success", true);
        //Main code

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(jsonObject));
    }

}
