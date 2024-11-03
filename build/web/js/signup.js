async function loadSignUp() {

    const user_dto = {
        first_name: document.getElementById("first-name").value,
        last_name: document.getElementById("last-name").value,
        email: document.getElementById("email-1").value,
        password: document.getElementById("password-1").value
    };

    const response = await fetch(
            "SignUp",
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
            window.location = "verify.html";
        } else {
            popup.error({
                message: json.content

            });
        }

    } else {
        popup.error({
            message: json.content

        });
    }

}
