const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/websocket'
});

stompClient.onConnect = (frame) => {
    console.log('Connected: ' + frame);
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
}
var subscription = null;
function retrieveProduct() {
    var sku = $("#sku").val()
    subscription = stompClient.subscribe('/topic/products/' + sku, (response) => {
         var event = JSON.parse(response.body)
         $("#product_events").append("<tr><td>" + event.createdAt + "</td><td>" + event.name + "</td><td>" + event.payload + "</td></tr>");
         var detail = JSON.parse(event.payload)
         switch (event.name) {
            case "ProductRestockedEvent":
                $("#product_quantity").text(parseInt($("#product_quantity").text()) + detail.quantity);
                break;
            case "ProductPurchasedEvent":
                $("#product_quantity").text(parseInt($("#product_quantity").text()) - detail.quantity);
                break;
            case "ProductPriceChangedEvent":
                $("#product_price").text(detail.price);
                break;

         }
    });
    $.ajax({
        url: "http://localhost:8080/v1/products/" + sku,
        type: "GET",
        success: function(data){
            $("#product_sku").text(data.sku)
            $("#product_name").text(data.name)
            $("#product_price").text(data.price)
            $("#product_quantity").text(data.quantity)
            $('#product_details').show();
            $("#watch").text('Stop Watching');
            $("#sku").prop('disabled', true);
        },
        error: function(data) {
            alert(data.responseJSON.message)
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    });
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    stompClient.activate();
    $("#watch").click(() => {
        if ($("#watch").text() === 'Start Watching') {
            retrieveProduct();
        } else {
            $("#sku").prop('disabled', false);
            $("#product_events").empty();
            $('#product_details').hide();
            $("#watch").text('Start Watching');
            subscription.unsubscribe();
            subscription = null;
        }
    });
});