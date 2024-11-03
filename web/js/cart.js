async function loadCartItems() {
    
    loadAllProductsToIndex();

    const response = await fetch(
            "LoadCartItems"
            );

    const popup = Notification();

    if (response.ok) {

        const json = await response.json();
        console.log(json);

        if (json.length === 0) {

            popup.error({
                message: "Your cart is empty."
            });


        } else {

            let  cartItemContainer = document.getElementById("cartItemContainer");
            let  cartItemRaw = document.getElementById("cartItemRaw");

            cartItemContainer.innerHTML = "";


            let totalQty = 0;
            let total = 0;

            json.forEach(item => {
                let itemSubTotal = item.product.price * item.qty;

                totalQty += item.qty;
                total += itemSubTotal;

                let cartItemRowClone = cartItemRaw.cloneNode(true);
                cartItemRowClone.querySelector("#cartItemImage").src = "product-images/" + item.product.id + "/image1.png";
                cartItemRowClone.querySelector("#cartItemA").href = "product-detail.html?id=" + item.product.id;
                cartItemRowClone.querySelector("#cartItemTitle").innerHTML = item.product.title;
                cartItemRowClone.querySelector("#cartItemPrice").innerHTML = "Rs. " + new Intl.NumberFormat().format(item.product.price);
                cartItemRowClone.querySelector("#cartItemQty").value = item.qty;
                cartItemRowClone.querySelector("#cartItemSubTotatl").innerHTML = "Rs. " + new Intl.NumberFormat().format(itemSubTotal);
                cartItemContainer.appendChild(cartItemRowClone);
            });

            document.getElementById("cartTotalQuantity").innerHTML = totalQty;
            document.getElementById("cartTotal").innerHTML = "Rs. " + new Intl.NumberFormat().format(total);
        }



    } else {
        popup.error({
            message: "Unable to Process your request"

        });
    }
}

async function loadAllProductsToIndex() {

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