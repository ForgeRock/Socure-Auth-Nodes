
try{
    var onfido_div = document.createElement("div");
    onfido_div.setAttribute("id", "socure");

    var config = {
        onProgress: onProgress, //callback method for reading the progress status
        onSuccess: onSuccess, //callback method to read the success response
        onError: onError, //callback method to read the error response
        qrCodeNeeded: true //toggle the QR code display
    };

    function onProgress() { };
    function onSuccess() { };
    function onError() { };

    var script = document.createElement('script');
    script.setAttribute("src", "https://websdk.socure.com/bundle.js");




    document.body.appendChild(onfido_div);
    document.body.appendChild(script);

    script.onload = function () {
        onfido = SocureInitializer.init("3b76f523-8982-45cb-9292-e1f053b1d7b1")
            .then(lib => {
                lib.init("3b76f523-8982-45cb-9292-e1f053b1d7b1", "#socure", config)
                    .then(function () {
                        lib.start(1);
                    });
            });

    };

      }catch(e){
      	console.log(e)
      }// window.setTimeout(jig(), 5000);
