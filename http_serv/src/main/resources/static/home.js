const BASE_PATH = "http://localhost:14555"
const API_GET_INPUT_FILES_PATHS_URL = BASE_PATH+ "/getInp";
const API_GET_OUTPUT_FILES_PATHS_URL = BASE_PATH+ "/getOut";
const API_GET_UPDATE_SITE = BASE_PATH + "/reloadsite";

const API_POST_UPDATE_FILE = BASE_PATH + "/updateFile";

const HEADER_GET_ANY_FILE = "PLEASE_GIVE_ME_A_FILE"
const HEADER_FILE_PATH = "FILE_PATH";

let currentlySelectedInTextArea=""

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
function httpPost(theUrl, body="", headers=[]) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("POST", theUrl, false); // false for synchronous request
    for(let x of headers){
        console.log(x['headerName'])
        console.log(x['headerValue'])
        xmlHttp.setRequestHeader(x['headerName'], x['headerValue']);
    }
    console.log("The body : "+body)
    xmlHttp.send(body);
    return xmlHttp.responseText;
}
/***********************************************************************************/
//Specific functions
function showFileFromServer(i,inputOrOutput){
    console.log("i")
    console.log(i)
    let x = inputOrOutput==='input' ? jsonInpFiles[i] : jsonOutFiles[i];
    let result = httpGet(x['absolutePath'], [{headerName:HEADER_GET_ANY_FILE, headerValue:'true'}])
    console.log("result");
    console.log(result)
    if(inputOrOutput==='input') {
        console.log("hellelleo")
        document.getElementById("textarea_update").value = result;
        let outputCorresponding = matchInputToOutput(x)
        if(outputCorresponding != undefined && outputCorresponding != null){
            let resultOutputCorresponding = httpGet(outputCorresponding['absolutePath'], [{headerName:HEADER_GET_ANY_FILE, headerValue:'true'}]);
            document.getElementById("show_div").innerHTML =resultOutputCorresponding;
        }
    }else{
        document.getElementById("show_div").innerHTML = result;
    }
}
function matchInputToOutput(x){
    for(let y of jsonOutFiles){
        if(y['absolutePath'].replace(y['absoluteBaseOutputPath'],"")===
            x['absolutePath'].replace(x['absoluteBaseInputPath']+"/content","").replace(".md",".html")){
            return y
        }
    }
}
function updateInputFile(){
    console.log("salut")
    console.log(currentlySelectedInTextArea)
    httpPost(API_POST_UPDATE_FILE,
        document.getElementById("textarea_update").value,
        currentlySelectedInTextArea!==undefined?
            [{headerName:HEADER_FILE_PATH, headerValue:currentlySelectedInTextArea['absolutePath']}]:
            []);
    httpGet(API_GET_UPDATE_SITE);
    showFileFromServer(jsonInpFiles.indexOf(currentlySelectedInTextArea), 'input')
}

/***************************************************************/
//Fill Input files list and render it
jsonInpFiles = JSON.parse(httpGet(API_GET_INPUT_FILES_PATHS_URL));
let res = "<ul>";
let i = 0;
for (let x of jsonInpFiles) {
    res += " <li> <a href='#' id='li_inp_" + i.toString() + "' >" + x['absolutePath'].replace(x['absoluteBaseInputPath'],"") + " </a> </li> "
    i++;
}
i--;
res += "</ul>";
document.getElementById("inp_files").innerHTML += res;
while(i>=0){
    document.getElementById('li_inp_'+i).addEventListener('click', event=>{
        console.log("coucou id "+event.target.id);
        let id = Number.parseInt(event.target.id.replace("li_inp_",""));
        currentlySelectedInTextArea = jsonInpFiles[id];
        showFileFromServer(id, "input");
    });
    i--;
}
//Fill output files list and render it
jsonOutFiles = JSON.parse(httpGet(API_GET_OUTPUT_FILES_PATHS_URL));
console.log("out ")
console.log(jsonOutFiles)
res = "<ul>";
i = 0;
for (let x of jsonOutFiles) {
    res += " <li> <a href='#' id='li_out_" + i.toString() + "' >" + x['absolutePath'].replace(x['absoluteBaseOutputPath'],"") + " </> </li> "
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

