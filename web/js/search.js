/* global category_name, condition_name, color_name, storage_name */

async function loadData() {

    const response = await fetch(
            "SearchProducts",
            );
    if (response.ok) {
        const popup = Notification();
        const json = await response.json();
        console.log(json);
        updateProductView(json);
        let categoryList = json.categoryList;
        //start load categorylist
        let category_options = document.getElementById("category-options");
        let category_li = document.getElementById("category-li");
        category_options.innerHTML = "";
        categoryList.forEach(category => {
            let category_li_clone = category_li.cloneNode(true);
            category_li_clone.querySelector("#category-a").innerHTML = category.name;
            category_options.appendChild(category_li_clone);
        });
        //start template js
        const categoryOptions = document.querySelectorAll('#category-options li');
        categoryOptions.forEach(option => {
            option.addEventListener('click', function () {
                categoryOptions.forEach(opt => opt.classList.remove('chosen'));
                this.classList.add('chosen');
            });
        });
        //end tempalte js

        // end category list

        //start load color list
        let colorList = json.colorList;
        let color_options = document.getElementById("color-options");
        let color_li = document.getElementById("color-li");
        color_options.innerHTML = "";
        colorList.forEach(color => {
            let color_li_clone = color_li.cloneNode(true);
            color_li_clone.querySelector("#color-a").style.background = color.name;
            color_li.style.borderColor = color.name;
            color_options.appendChild(color_li_clone);
        });
        //start template
        const colorOptions = document.querySelectorAll('#color-options li');
        colorOptions.forEach(option => {
            option.addEventListener('click', function () {
                colorOptions.forEach(opt => opt.classList.remove('chosen'));
                this.classList.add('chosen');
            });
        });
        //end template

        //end load color list

        //start load storage list
        let sizeList = json.sizeList;
        let size_options = document.getElementById("storage-options");
        let size_li = document.getElementById("storage-li");
        size_options.innerHTML = "";
        sizeList.forEach(storage => {
            let size_li_clone = size_li.cloneNode(true);
            size_li_clone.querySelector("#storage-a").innerHTML = storage.name;
            size_options.appendChild(size_li_clone);
        });
        //start template


        const storageOptions = document.querySelectorAll('#storage-options li');
        storageOptions.forEach(option => {
            option.addEventListener('click', function () {
                storageOptions.forEach(opt => opt.classList.remove('chosen'));
                this.classList.add('chosen');
            });
        });
        //end template

        //end load storage list




    } else {
        popup.success({
            message: "Try again Later"
        });
    }
}

//function loadOption(prefix, dataList) {
//    let options = document.getElementById(prefix + "-options");
//    let li = document.getElementById(prefix +"");
//    options.innerHTML = "";
//
//    dataList.forEach(data => {
//        let li_clone = li.cloneNode(true);
//        li_clone.querySelector("#" + prefix + "-a").innerHTML = data.name;
//        options.appendChild(li_clone);
//    });
//
//    const all_li = document.querySelectorAll("#" + prefix + "-options li");
//    all_li.forEach(option => {
//        option.addEventListener('click', function () {
//            all_li.forEach(opt => opt.classList.remove('chosen'));
//            this.classList.add('chosen');
//        });
//    });
//}

async function searchProdcts(firstResult) {


    const popup = Notification();
    //get selected category
    const category_name =
            document.getElementById("category-options")
            .querySelector(".chosen")
            ?.querySelector("a").innerHTML;
    console.log(category_name);

    //get selected color
    const color_name =
            document.getElementById("color-options")
            .querySelector(".chosen")
            ?.querySelector("a").style.backgroundColor;
    console.log(color_name);
    //get selected condition
    const storage_name =
            document.getElementById("storage-options")
            .querySelector(".chosen")
            ?.querySelector("a").innerHTML;
    console.log(storage_name);

    let price_range_start = document.getElementById("startPriceRange").value;
    let price_range_end = document.getElementById("stopPriceRange").value;

    let sort_text = document.getElementById("sort-text").value;
    console.log(sort_text);

    const data = {
        firstResult: firstResult,
        category_name: category_name,
        color_name: color_name,
        storage_name: storage_name,
        price_range_start: price_range_start,
        price_range_end: price_range_end,
        sort_text: sort_text
    };
    const response = await fetch(
            "SearchProductsAll",
            {
                method: "POST",
                body: JSON.stringify(data),
                headers: {
                    "Content-Type": "application/json"
                }
            }

    );
    if (response.ok) {

        const json = await response.json();
        console.log(json);
        if (json.success) {

            updateProductView(json);
            //currentPage = 0;

            popup.success({
                message: "Search Completed"

            });
        } else {
            popup.error({
                message: "Try againg later"

            });
        }

    } else {
        popup.error({
            message: "Try againg later"

        });
    }
}

var st_product = document.getElementById("st-product");
var currentPage = 0;
let pagination_button = document.getElementById("pagination-button");
function updateProductView(json) {
    //start load product

    let product_container = document.getElementById("st-product-container");
    product_container.innerHTML = "";
    json.productList.forEach(product => {

        let product_clone = st_product.cloneNode(true);
        //update details
        product_clone.querySelector("#st-product-a").href = "single-product.html?id=" + product.id;
        product_clone.querySelector("#st-product-img-1").src = "product-images/" + product.id + "/image1.png";
        product_clone.querySelector("#st-product-a2").href = "single-product.html?id=" + product.id;
        product_clone.querySelector("#st-product-title").innerHTML = product.title;
        product_clone.querySelector("#st-product-price").innerHTML = new Intl.NumberFormat().format(product.price);
        product_container.appendChild(product_clone);
    });
    //start pagination

    let st_pagination_container = document.getElementById("st-pagination-container");

    st_pagination_container.innerHTML = "";
    let product_count = json.allProductCount;
    const product_per_page = 5;
    let pages = Math.ceil(product_count / product_per_page);
    //add previous button

    if (currentPage != 0) {
        let pagination_prevbutton_clone = pagination_button.cloneNode(true);
        pagination_prevbutton_clone.innerHTML = "Prev";
        pagination_prevbutton_clone.addEventListener("click", e => {
            currentPage--;
            searchProdcts(currentPage * 5);
        });
        st_pagination_container.appendChild(pagination_prevbutton_clone);
    }

    //add page buttons
    for (let i = 0; i < pages; i++) {
        let pagination_button_clone = pagination_button.cloneNode(true);
        pagination_button_clone.innerHTML = i + 1;
        pagination_button_clone.addEventListener("click", e => {
            currentPage = i;
            searchProdcts(i * 5);
        });
        if (i === currentPage) {
            pagination_button_clone.className = "btn btn-danger ml--10";
        } else {
            pagination_button_clone.className = "btn btn-primary ml--10";
        }

        st_pagination_container.appendChild(pagination_button_clone);
    }

    //add next button

    if (currentPage != pages - 1) {
        let pagination_nextbutton_clone = pagination_button.cloneNode(true);
        pagination_nextbutton_clone.innerHTML = "Next";
        pagination_nextbutton_clone.addEventListener("click", e => {
            currentPage++;
            searchProdcts(currentPage * 5);
        });
        st_pagination_container.appendChild(pagination_nextbutton_clone);
    }


    //end pagination

}