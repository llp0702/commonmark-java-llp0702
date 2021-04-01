const BASE_PATH = "http://localhost:14555"
const API_GET_INPUT_FILES_PATHS_URL = BASE_PATH+ "/getInp";
const API_GET_OUTPUT_FILES_PATHS_URL = BASE_PATH+ "/getOut";
const HEADER_GET_ANY_FILE = "PLEASE_GIVE_ME_A_FILE"

/****************************************************************/
//Global functions
function httpGet(theUrl, headers=[]) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", theUrl, false); // false for synchronous request
    for(let x of headers){
        console.log(x['headerName'])
        console.log(x['headerValue'])
        xmlHttp.setRequestHeader(x['headerName'], x['headerValue']);
    }
    xmlHttp.send(null);
    return xmlHttp.responseText;
}
/***********************************************************************************/
//Specific functions
function showFileFromServer(i,inputOrOutput){
    console.log("i")
    console.log(i)
    let x = jsonInpFiles[i]
    let result = httpGet(x['absolutePath'], [{headerName:HEADER_GET_ANY_FILE, headerValue:'true'}])
    console.log("result");
    console.log(result)
    if(inputOrOutput==='input') {
        document.getElementById("textarea_update").innerText = result;
    }else{
        document.getElementById("show_div").innerHTML = result;
    }
}


/***************************************************************/
//Fill Input files list and render it
jsonInpFiles = JSON.parse(httpGet(API_GET_INPUT_FILES_PATHS_URL));
let res = "<ul>";
let i = 0;
for (let x of jsonInpFiles) {
    res += " <li> <a href='#' id='li_inp_" + i.toString() + "' >" + x['name'] + " </a> </li> "
    i++;
}
i--;
res += "</ul>";
document.getElementById("inp_files").innerHTML += res;
while(i>=0){
    document.getElementById('li_inp_'+i).addEventListener('click', event=>{
        console.log("coucou id "+event.target.id);
        let id = Number.parseInt(event.target.id.replace("li_inp_",""));
        showFileFromServer(id, "input");
    });
    i--;
}
//Fill output files list and render it
jsonOutFiles = JSON.parse(httpGet(API_GET_OUTPUT_FILES_PATHS_URL));
res = "<ul>";
i = 0;
for (let x of jsonOutFiles) {
    res += " <li> <a href='#' id='li_out_" + i.toString() + "' >" + x['name'] + " </> </li> "
    i++;
}
i--;
res += "</ul>"
document.getElementById("out_files").innerHTML += res;
while(i>=0){
    document.getElementById('li_out_'+i).addEventListener('click', event=>{
        console.log("coucou id "+event.target.id);
        let id = Number.parseInt(event.target.id.replace("li_out_",""));
        showFileFromServer(id, "output");
    });
    i--;
}

