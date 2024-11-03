async function verifyAccount() {

    const dto = {
        verification: document.getElementById("verificationCode").value
    };

    const response = await fetch(
            "Verification",
            {
                method: "POST",
                body: JSON.stringify(dto),
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
            window.location = "signup.html";
        } else {
            popup.error({
                message: json.content

            });
        }

    } else {
        popup.error({
                message: "Please try again"

            });
    }

}



