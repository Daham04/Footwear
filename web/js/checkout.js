const popup = Notification();

// Payment completed. It can be a successful failure.
payhere.onCompleted = function onCompleted(orderId) {
    console.log("Payment completed. OrderID:" + orderId);
    // Note: validate the payment and show success or failure page to the customer
    popup.success({
        message: "Thank you, Payment completed!"
    });
    window.location = "index.html";
};

// Payment window closed
payhere.onDismissed = function onDismissed() {
    // Note: Prompt user to pay again or show an error page
    console.log("Payment dismissed");
};

// Error occurred
payhere.onError = function onError(error) {
    // Note: show an error page
    console.log("Error:" + error);
};


//**********************pah here end***************************//

async function loadData() {

    const response = await fetch(
            "LoadCheckOut"
            );

    if (response.ok) {

        const json = await response.json();
        console.log(json);

        if (json.success) {

            //store response data
            const address = json.address;
            const cityList = json.cityList;
            const cartList = json.cartList;

            //load cities
            const citySelect = document.getElementById("cityList");
            loadCities(cityList, "name");

            //load current address
            let currentAddressCheckBox = document.getElementById("checkbox1");
            currentAddressCheckBox.addEventListener("change", e => {

                let fname = document.getElementById("firstName");
                let lname = document.getElementById("lastName");
                let city = document.getElementById("cityList");
                let address1 = document.getElementById("address1");
                let address2 = document.getElementById("address2");
                let pcode = document.getElementById("postal-code");
                let mobile = document.getElementById("mobile");

                if (currentAddressCheckBox.checked) {
                    fname.value = address.first_name;
                    lname.value = address.last_name;
                    city.value = address.city.id;
                    city.disabled = true;
                    city.dispatchEvent(new Event("change"));
                    ;
                    address1.value = address.line1;
                    address2.value = address.line2;
                    pcode.value = address.postal_code;
                    mobile.value = address.mobile;
                } else {
                    fname.value = "";
                    lname.value = "";
                    city.value = 0;
                    city.disabled = false;
                    city.dispatchEvent(new Event("change"));
                    address1.value = "";
                    address2.value = "";
                    pcode.value = "";
                    mobile.value = "";
                }
            });

            //load cart items 
            let st_tbody = document.getElementById("st-tbody");
            let st_item_tr = document.getElementById("st-item-tr");
            let st_order_sub_total_tr = document.getElementById("st-order-subtotal-tr");
            let st_order_shipping_tr = document.getElementById("st-order-shipping-tr");
            let st_order_total_tr = document.getElementById("st-order-total-tr");

            st_tbody.innerHTML = "";

            let sub_total = 0;

            cartList.forEach(cart => {

                let st_item_clone = st_item_tr.cloneNode(true);
                st_item_clone.querySelector("#st-item-title").innerHTML = cart.product.title;
                st_item_clone.querySelector("#ch-product-qty").innerHTML = cart.qty;

                let item_sub_total = cart.product.price * cart.qty;
                sub_total += item_sub_total;

                st_item_clone.querySelector("#st-item-subtotal").innerHTML = new Intl.NumberFormat().format(item_sub_total);

                st_tbody.appendChild(st_item_clone);


            });
            st_order_sub_total_tr.querySelector("#st-subtotal").innerHTML = new Intl.NumberFormat().format(sub_total);
            st_tbody.appendChild(st_order_sub_total_tr);

            //update shipping charges on city change
            citySelect.addEventListener("change", e => {
                //update shipping chargers
                let item_count = cartList.length;

                let shipping_ammount = 0;

                //check city colombo or not

                if (citySelect.value === 1) {
                    //colombo
                    shipping_ammount = item_count * 1000;
                } else {
                    //out of colombo
                    shipping_ammount = item_count * 2500;
                }

                st_order_shipping_tr.querySelector("#st-shipping-amount").innerHTML = new Intl.NumberFormat().format(shipping_ammount);
                st_tbody.appendChild(st_order_shipping_tr);

                let total = sub_total + shipping_ammount;
                st_order_total_tr.querySelector("#st-total").innerHTML = new Intl.NumberFormat().format(total);
                st_tbody.appendChild(st_order_total_tr);

            });

        } else {

            window.location = "signup.html";

        }
    }
}

function loadCities(cityList, property) {

    //console.log(cityList);

    const cityTag = document.getElementById("cityList");
    cityList.forEach(item => {
        let optionTag = document.createElement("option");
        optionTag.value = item.id;
        optionTag.innerHTML = item[property];
        cityTag.appendChild(optionTag);
    });
}


async function checkOut() {

    let isCurrentAddress = document.getElementById("checkbox1").checked;

    //get address data

    let fname = document.getElementById("firstName");
    let lname = document.getElementById("lastName");
    let city = document.getElementById("cityList");
    let address1 = document.getElementById("address1");
    let address2 = document.getElementById("address2");
    let pcode = document.getElementById("postal-code");
    let mobile = document.getElementById("mobile");

    //request data(json)
    const data = {

        isCurrentAddress: isCurrentAddress,
        first_name: fname.value,
        last_name: lname.value,
        city_id: city.value,
        address1: address1.value,
        address2: address2.value,
        postal_code: pcode.value,
        mobile: mobile.value
    };


    const response = await fetch(
            "Checkout1",
            {
                method: "POST",
                body: JSON.stringify(data),
                headers: {
                    "Content-Type": "application/json"
                }
            }

    );
    const popup = Notification();

    if (response.ok) {

        const json = await response.json();

        if (json.success) {
            console.log(json.payhereJson);
            payhere.startPayment(json.payhereJson);

        } else {
            popup.error({
                message: json.message
            });

        }
    } else {
        popup.error({
            message: "Try again later"
        });

    }
}
