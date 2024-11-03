var modelList;
async function loadFeatures() {
    const popup = Notification();

    const response = await fetch(
            "LoadFeatures"
            );
    if (response.ok) {

        const json = await response.json();

        const categoryList = json.categoryList;
        modelList = json.modelList;
        const colorList = json.colorList;
        const sizeList = json.sizeList;
        loadSelect("category-select", categoryList, "name");
        //loadSelect("model-select", modelList, "name");
        loadSelect("color-select", colorList, "name");
        loadSelect("size-select", sizeList, "name");
    } else {
        popup.error({
            mesaage: "Please Try Again"
        });
    }

}

function loadSelect(selectTagId, list, property) {

    const selectTag = document.getElementById(selectTagId);
    list.forEach(item => {
        let optionTag = document.createElement("option");
        optionTag.value = item.id;
        optionTag.innerHTML = item[property];
        selectTag.appendChild(optionTag);
    });
}

function updateModels() {

    let modelSelectTag = document.getElementById("model-select");
    modelSelectTag.length = 1;
    let selectedCategoryId = document.getElementById("category-select").value;
    modelList.forEach(model => {
        if (model.category.id == selectedCategoryId) {
            let optionTag = document.createElement("option");
            optionTag.value = model.id;
            optionTag.innerHTML = model.name;
            modelSelectTag.appendChild(optionTag);
        }
    });
}

async function productListining() {
    const popup = Notification();

    const categorySelect = document.getElementById("category-select");
    const modelSelect = document.getElementById("model-select");
    const title = document.getElementById("title");
    const description = document.getElementById("description");
    const sizeSelect = document.getElementById("size-select");
    const colorSelect = document.getElementById("color-select");
    const price = document.getElementById("price");
    const quantity = document.getElementById("quantity");
    const image1 = document.getElementById("image1");
    const image2 = document.getElementById("image2");
    const image3 = document.getElementById("image3");
    
    const data = new FormData();
    data.append("categoryId", categorySelect.value);
    data.append("modelId", modelSelect.value);
    data.append("title", title.value);
    data.append("description", description.value);
    data.append("sizeId", sizeSelect.value);
    data.append("colorSelectId", colorSelect.value);
    data.append("price", price.value);
    data.append("quantity", quantity.value);
    data.append("image1", image1.files[0]);
    data.append("image2", image2.files[0]);
    data.append("image3", image3.files[0]);

    var response = await fetch(
            "ProductListing1",
            {
                method: "POST",
                body: data
            }
    );
    if (response.ok) {

        const json = await response.json();


        if (json.success) {

            categorySelect.value = 0;
            modelSelect.length = 1;
            title.value = "";
            description.value = "";
            sizeSelect.value = 0;
            colorSelect.value = 0;
            price.value = "";
            quantity.value = 1;
            image1.value = null;
            image2.value = null;
            image3.value = null;

            popup.success({
                mesaage: json.content
            });

        } else {

            popup.error({
                mesaage: json.content
            });
        }

    } else {

        popup.error({
            mesaage: "Please Try Again Later"
        });
    }


}

