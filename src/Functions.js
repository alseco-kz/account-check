function Integration_Test_Google(){
    try {
        var response = $integration.customRequest(
                "75ccb038-dd32-41fe-8328-122699d2bedc",
                "https://sheets.googleapis.com/v4/spreadsheets/" + "1mCVy2yB4oBFjpzJAOiyduII2ZzatrZgAmbRBXsXRJqQ" + "/values/O1:O",
                "GET",
                null,
                null
            );}
    catch(e){
            //$reactions.answer("Что-то сервер барахлит. ");
            log('--------------- произошла ошибка' );
            SendErrorMessage("onHttpRequest", 'Функция: Integration_Test_Google')
            return false;
        }; 
    if (response.IsOk){
        var data = response.data
            
        }
        return response.responseBody
    }
function Return_Count(row){
    try {
        var response_count = $integration.customRequest(
                "75ccb038-dd32-41fe-8328-122699d2bedc",
                "https://sheets.googleapis.com/v4/spreadsheets/" + "1mCVy2yB4oBFjpzJAOiyduII2ZzatrZgAmbRBXsXRJqQ" + "/values/K1:K",
                "GET",
                null,
                null
            );}
    catch(e){
            //$reactions.answer("Что-то сервер барахлит. ");
            log('--------------- произошла ошибка' );
            SendErrorMessage("onHttpRequest", 'Функция: Return_Count')
            return false;
        }; 
    var value_count  = JSON.parse(response_count.responseBody).values
    return Number(value_count[row]) +1
    
        
}
function getMonthName(monthNumber) {
  var date = new Date();
  date = date.setMonth(monthNumber - 1);

  date.toLocaleString('ru-RU', { month: 'long'});
  var monthNames = [
  "январь", "февраль", "март", "апрель", "май", "июнь",
  "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"];
  var monthIndex = new Date(date).getMonth(); // Get the month index (0-11)
  var monthName = monthNames[monthIndex];
  return monthName;
}

function sleep(milliseconds) {
  var date = Date.now();
  var currentDate = null;
  do {
    currentDate = Date.now();
  } while (currentDate - date < milliseconds);
}


function getMonthNameKZ(monthNumber) {
  var date = new Date();
  date = date.setMonth(monthNumber - 1);

  date.toLocaleString('kk-KK', { month: 'long'});
  var monthNames = [
  "Қаңтар", "Ақпан", "Наурыз", "Сәуір", "Мамыр", "Маусым",
  "Шiлде", "Тамыз", "Қыркүйек", "Қазан", "Қараша", "Желтоқсан"];
  var monthIndex = new Date(date).getMonth(); // Get the month index (0-11)
  var monthName = monthNames[monthIndex];
  return monthName;
}
var patterns_words = ['не понимаю','қазақша','орша тусынбейм', 'орыс тілінде Мен түсінбеймін','түсінбеймін', 'казахша', 'казакша сойлейш', 'сойлейш', 'по казахски', 'на казахском', 'казах','қазақ', 'саламатсыз' , 'cәлеметсіз', 'сәлеметсіз бе' ];

var columns = ['лицевой_счет','населенный_пункт',	'тип_улицы_микрорайона',	'улица',	'дом', 'квартира',	'номер_телефона',	'реестр_печати_по_последней_печати',	'способ_доставки'];
function getPayLoad(){
    
    
    if($dialer.getPayload().лицевой_счет)
        $analytics.setSessionData("лицевой_счет", $dialer.getPayload().лицевой_счет);
    
    if($dialer.getPayload().населенный_пункт)
        $analytics.setSessionData("населенный_пункт", $dialer.getPayload().населенный_пункт);
        

    if($dialer.getPayload().тип_улицы_микрорайона)
        $analytics.setSessionData("тип_улицы_микрорайона", $dialer.getPayload().тип_улицы_микрорайона);
        
    if($dialer.getPayload().улица)
        $analytics.setSessionData("улица", $dialer.getPayload().улица);    
        
    if($dialer.getPayload().дом)
        $analytics.setSessionData("дом", $dialer.getPayload().дом);   
        
    if($dialer.getPayload().квартира)
        $analytics.setSessionData("квартира", $dialer.getPayload().квартира);    
        
    if($dialer.getPayload().номер_телефона)
        $analytics.setSessionData("номер_телефона", $dialer.getPayload().номер_телефона);   
        
        
    if($dialer.getPayload().реестр_печати_по_последней_печати)
        $analytics.setSessionData("реестр_печати_по_последней_печати", $dialer.getPayload().реестр_печати_по_последней_печати);   
        
    if($dialer.getPayload().способ_доставки)
        $analytics.setSessionData("способ_доставки", $dialer.getPayload().способ_доставки);       
    
    
    $analytics.setSessionData("Время", '-');     
    
    $analytics.setSessionData("Результат", 'NaN');   
    
    $analytics.setSessionData("Дата Получения", '-');   
    
    
  
}