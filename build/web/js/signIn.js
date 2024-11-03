async function signIn() {

    const user_dto = {
        email: document.getElementById("email-2").value,
        password: document.getElementById("password-2").value
    };


    const response = await fetch(
            "SignIn",
            {
                method: "POST",
                body: JSON.stringify(user_dto),
                headers: {
                    "Content-Type": "application/json"
                }
            }

    );

    if (response.ok) {

        const json = await response.json();
        const popup = Notification();

        if (json.success) {
            popup.success({
                message: json.content

            });
            window.location = "index.html";
        } else {
            if (json.content === "Unverified") {
                window.location = "verify-account.html";
            } else {
                popup.error({
                message: json.content

            });
            }
        }

    } else {
          
    }

}



