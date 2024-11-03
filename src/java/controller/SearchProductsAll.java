package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Category;
import entity.Color;
import entity.Model;
import entity.Product;
import entity.Product_Status;
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
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SearchProductsAll", urlPatterns = {"/SearchProductsAll"})
public class SearchProductsAll extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject respoJsonObject = new JsonObject();
        respoJsonObject.addProperty("success", false);

        //get requset
        JsonObject requsetJsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        Session session = HibernateUtil.getSessionFactory().openSession();

        //search all products
        Criteria criteria1 = session.createCriteria(Product.class);

        //add category filter
        if (requsetJsonObject.has("category_name")) {
            //Category selected
            String category_name = requsetJsonObject.get("category_name").getAsString();
            //get category from db
            Criteria criteria2 = session.createCriteria(Category.class);
            criteria2.add(Restrictions.eq("name", category_name));
            Category category = (Category) criteria2.uniqueResult();

            //filter model by categoey
            Criteria criteria3 = session.createCriteria(Model.class);
            criteria3.add(Restrictions.eq("category", category));
            List<Model> modelList = criteria3.list();

            //filter produts by model from db
            criteria1.add(Restrictions.in("model", modelList));
        }

        if (requsetJsonObject.has("color_name")) {
            String color_name = requsetJsonObject.get("color_name").getAsString();

            //get color db
            Criteria criteria5 = session.createCriteria(Color.class);
            criteria5.add(Restrictions.eq("name", color_name));
            Color color = (Color) criteria5.uniqueResult();

            //filter products by color
            criteria1.add(Restrictions.eq("color", color));
        }

        if (requsetJsonObject.has("storage_name")) {
            String storage_name = requsetJsonObject.get("storage_name").getAsString();

            //get color db
            Criteria criteria6 = session.createCriteria(Size.class);
            criteria6.add(Restrictions.eq("value", storage_name));
            Size storage = (Size) criteria6.uniqueResult();

            //filter products by storage
            criteria1.add(Restrictions.eq("storage", storage));
        }

        //filter products by price
        double price_range_start = requsetJsonObject.get("price_range_start").getAsDouble();
        double price_range_end = requsetJsonObject.get("price_range_end").getAsDouble();

        criteria1.add(Restrictions.ge("price", price_range_start));
        criteria1.add(Restrictions.le("price", price_range_end));

        //filter products by sort opions
        String sort_text = requsetJsonObject.get("sort_text").getAsString();

        if (sort_text.equals("Sort by Latest")) {
            criteria1.addOrder(Order.desc("id"));
        } else if (sort_text.equals("Sort by Oldest")) {
            criteria1.addOrder(Order.asc("id"));
        } else if (sort_text.equals("Sort by Name")) {
            criteria1.addOrder(Order.asc("title"));
        } else if (sort_text.equals("Sort by Price")) {
            criteria1.addOrder(Order.asc("price"));
        }
        //get all products count
        respoJsonObject.addProperty("allProductCount", criteria1.list().size());

        //set product range
        int firstResult = requsetJsonObject.get("firstResult").getAsInt();
        criteria1.setFirstResult(firstResult);
        criteria1.setMaxResults(5);

        //get products list
        List<Product> productList = criteria1.list();

        //remove user product
        for (Product product : productList) {
            product.setUser(null);
        }

        respoJsonObject.addProperty("success", true);
        respoJsonObject.add("productList", gson.toJsonTree(productList));

        //send response 
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(respoJsonObject));
    }

}
