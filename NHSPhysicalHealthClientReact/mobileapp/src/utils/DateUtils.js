export const dateToStateString = (date) => {
    var datestring = date.getFullYear() + "-" + ("0"+(date.getMonth()+1)).slice(-2) + "-" + ("0" + date.getDate()).slice(-2);
    return datestring;
}

export const getLocalDateString = (date) => {
    // Return date in UK date format dd/mm/yy
    var datestring = ("0" + date.getDate()).slice(-2) + "-" + ("0"+(date.getMonth()+1)).slice(-2) + "-" +  date.getFullYear();
    return datestring;
  }

  export const getLocalTimeString = (date) => {
    // Return date in UK date format dd/mm/yy
    var datestring = date.getHours() + ":" + ("0" + date.getMinutes()).slice(-2);
    return datestring;
  }

  export const getLocalDateTimeString = (date) => {
    // Return date in UK date format dd/mm/yy
    var datestring = getLocalDateString(date) + "  " + getLocalTimeString(date);
    return datestring;
  }
  
