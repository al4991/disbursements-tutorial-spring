$(document).ready(function() {
    // form
    $("#non-card-details").hide();
    $("#scheme").change(function () {
        var uriScheme = $(this).val();
        if (uriScheme != "PAN") {
            $("#card-details").hide();
            $("#expiry-year").removeAttr("required");
            $("#expiry-month").removeAttr("required");
            $("#name-on-account").attr("required", "");
            $("#non-card-details").show();
        } else {
            $("#non-card-details").hide();
            $("#name-on-account").removeAttr("required");
            $("#expiry-year").attr("required", "");
            $("#expiry-month").attr("required", "");
            $("#card-details").show();
        }
    })

    // alert success
    $(".json").hide();
    $("#show-request").click(function() {
        $(".json").slideDown("1000");
        $("#response").hide();
        $("#request").show();
    })

    $("#show-response").click(function() {
        $(".json").slideDown("1000");
        $("#request").hide();
        $("#response").show();
    })

    $("#reload").click(function(){
        location.reload(true);
    })

    $("#close").click(function() {
        $(".json").slideUp("1000");
    })
});