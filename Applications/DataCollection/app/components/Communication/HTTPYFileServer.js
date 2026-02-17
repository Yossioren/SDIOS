const serverAddress = "http://192.168.1.15";
XHR_Got200 = 0;
XHR_sent = 0;

export function getSucceeded(){
    return XHR_Got200;
}

export function getSent(){
    return XHR_sent;
}

function sendViaXHR(obj, then){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", serverAddress, true);
    xhr.setRequestHeader("Content-Type", "application/json");//TODO check with application/text

    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
            //console.error(xhr.response)
            XHR_Got200++;
            if(then !== undefined)
                then();
            console.log(obj.path, ':', xhr.response, ':', obj.data===undefined?'len?':obj.data.length);
        }
    }
    xhr.onerror = function(e) {
        console.log('onerror', e);
        //if(e.isTrusted === undefined)
        setTimeout(function(){ sendViaXHR(obj, then); }, 5000);
    }
    xhr.onabort = function(e) {
        console.error('onabort', e);
    }
    //without on error handler
    XHR_sent++;
    xhr.send(JSON.stringify(obj));
}

export function sendThisMessure(activity, ID, sensorType, sensorEvents, then){
    sendViaXHR({path:`${ID}/${sensorType}`, filename:activity, data:JSON.stringify(sensorEvents)}, then);
}

export function createNewStudent(studentID, phoneModel){
    console.log('HTTP_file_server create new student:'+studentID);
    sendViaXHR({path:studentID, filename:'phoneModel.json', data:JSON.stringify(phoneModel)});
}
