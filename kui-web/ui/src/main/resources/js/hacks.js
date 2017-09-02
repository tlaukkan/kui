function getDateFromMilliseconds(milliseconds) {
    var date = new Date()
    date.setTime(milliseconds)
    return date
}

function jsonStringToDate(dateString) {
    return new Date(dateString)
}

function dateStringToLongString(dateString) {
    return new Date(dateString).getTime().toString()
}

function base64Encode(str) {
    return btoa(str)
}

function escapeUri(str) {
    return encodeURIComponent(str).replace(/[-_.!~*'()]/g, function(c) {
        return '%' + c.charCodeAt(0).toString(16);
    });
}

// Use the browser's built-in functionality to quickly and safely escape
// the string
function escapeHtml(str) {
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
}

// UNSAFE with unsafe strings; only use on previously-escaped ones!
function unescapeHtml(escapedStr) {
    var div = document.createElement('div');
    div.innerHTML = escapedStr;
    var child = div.childNodes[0];
    return child ? child.nodeValue : '';
}

function showModal(id) {
    $('#' + id).modal()
}

function hideModal(id) {
    return $('#' + id).modal('hide')
}