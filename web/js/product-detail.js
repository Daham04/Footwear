async function loadProductDetails() {

    const parameters = new URLSearchParams(window.location.search);
    if (parameters.has("id")) {
        const productId = parameters.get("id");
        console.log(productId);
        const response = await fetch("LoadSingleProductDetails?id=" + productId);
        if (response.ok) {

            const json = await response.json();
            console.log(json.product.id);

            const id = json.product.id;
            document.getElementById("image1").src = "product-images/" + id + "/image1.png";
            document.getElementById("image2").src = "product-images/" + id + "/image2.png";
            document.getElementById("image3").src = "product-images/" + id + "/image3.png";
            document.getElementById("product-title").innerHTML = " " + json.product.title;
            document.getElementById("productPublishOn").innerHTML = " " + json.product.date_time;
            document.getElementById("productPrice").innerHTML = "Rs. " + new Intl.NumberFormat().format(json.product.price);
            document.getElementById("productCategory").innerHTML = " " + json.product.model.category.name;
            document.getElementById("productModel").innerHTML = " " + json.product.model.name;
            document.getElementById("productQuantity").innerHTML = " " + json.product.qty;
            document.getElementById("productColor").innerHTML = " " + json.product.color.name;
            document.getElementById("productSize").innerHTML = " " + json.product.size.name;
            document.getElementById("productDescription").innerHTML = json.product.description;
            document.getElementById("addToCartMain").addEventListener("click",
                    (e) => {
                addToCart(
                        json.product.id,
                        document.getElementById("addToCartQty").value
                        );
                e.preventDefault();
            });

             let ProductHtml = document.getElementById("similer-product");
            document.getElementById("similarProductMain").innerHTML = "";
            json.productList.forEach(item => {


                let productCloneHtml = ProductHtml.cloneNode(true);
                productCloneHtml.querySelector("#similarProductImage").src = "product-images/" + item.id + "/image1.png";
                productCloneHtml.querySelector("#similaProductA1").href = "product-detail.html?id=" + item.id;
                productCloneHtml.querySelector("#similaProductTitle").innerHTML = item.title;
                productCloneHtml.querySelector("#similaProductSize").innerHTML = item.size.name;
                productCloneHtml.querySelector("#similaProductPrice").innerHTML = "Rs. " + new Intl.NumberFormat().format(item.price);
                productCloneHtml.querySelector("#similaProductColor").innerHTML = item.color.name;
                productCloneHtml.querySelector("#similarProductAtToCart").addEventListener("click",
                        (e) => {
                    addToCart(item.id, 1);
                    e.preventDefault();
                });
                //Change other tags


                document.getElementById("similarProductMain").appendChild(productCloneHtml);
            });
            $('.recent-product-activation').slick({
                infinite: true,
                slidesToShow: 4,
                slidesToScroll: 4,
                arrows: true,
                dots: false,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
                responsive: [{
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 3,
                            slidesToScroll: 3
                        }
                    },
                    {
                        breakpoint: 991,
                        settings: {
                            slidesToShow: 2,
                            slidesToScroll: 2
                        }
                    },
                    {
                        breakpoint: 479,
                        settings: {
                            slidesToShow: 1,
                            slidesToScroll: 1
                        }
                    }
                ]
            });
            $('.header-campaign-activation').slick({
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: true,
                dots: false,
                autoplay: true,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>'
            });
            $('.testimonial-slick-activation-two').slick({
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: true,
                dots: true,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>'
            });
        } else {
            window.location = "index.html";
        }

    } else {
        window.location = "index.html";
    }
}

async function addToCart(id, qty) {

    const popup = Notification();

    const response = await fetch(
            "AddToCart?id=" + id + "&qty=" + qty,
            );

    if (response.ok) {

        const json = await response.json();



        if (json.success) {

            popup.success({
                message: json.content

            });
        } else {

            popup.error({
                message: json.content

            });
        }
    } else {
        popup.error({
            message: "Unable to Process your request"

        });
    }

}
