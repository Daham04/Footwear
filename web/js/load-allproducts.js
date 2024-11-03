async function loadAllProductsToIndex() {
    checkSignIn();

    const response = await fetch("LoadIndexProduct");
    if (response.ok) {
        const json = await response.json();
        console.log(json);
        let ProductHtml = document.getElementById("similer-product");
        document.getElementById("similarProductMain").innerHTML = "";
        json.productList.forEach(item => {


            let productCloneHtml = ProductHtml.cloneNode(true);
            productCloneHtml.querySelector("#similarProductImage").src = "product-images/" + item.id + "/image1.png";
            productCloneHtml.querySelector("#similaProductA1").href = "product-detail.html?id=" + item.id;
            productCloneHtml.querySelector("#similaProductA2").href = "product-detail.html?id=" + item.id;
            productCloneHtml.querySelector("#similaProductTitle").innerHTML = item.title;
            productCloneHtml.querySelector("#similaProductSize").innerHTML = "Size " + item.size.name;
            productCloneHtml.querySelector("#similaProductPrice").innerHTML = "Rs. " + new Intl.NumberFormat().format(item.price);
            productCloneHtml.querySelector("#similaProductColor").innerHTML = item.color.name;

            //Change other tags


            document.getElementById("similarProductMain").appendChild(productCloneHtml);
        });
    } else {
        console.log(json);
    }
}

async function checkSignIn() {

    const response = await fetch(
            "CheckSignIn",
            );

    if (response.ok) {

        const json = await response.json();
        console.log(json);

        const response_dto = json.response;


        if (response_dto.success) {
            //signed in

            const user = response_dto.content;
//            let user_main = document.getElementById("user-main");
//            let user_name = document.getElementById("user-name");
//            user_name.innerHTML = user.first_name + " " + user.last_name;
//            user_main.appendChild(user_name);

            let st_button_1 = document.getElementById("st-button-1");
            let st_button_main = document.getElementById("st-button-main");
            st_button_1.href = "SignOut";
            st_button_1.innerHTML = "Sign Out";
            st_button_main.innerHTML = "";

        } else {
            //not signed in
            console.log("Not signed in");
        }
        const productList = json.product;

    }
}

function changeView(){
    window.location = "search-products.html";
}