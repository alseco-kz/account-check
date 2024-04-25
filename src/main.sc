
require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: patterns.sc
  module = sys.zb-common
require: common.js
  module = sys.zb-common
require: dateTime/dateTime.sc
  module = sys.zb-common  
require: init.js 
require: Functions.js  
theme: /

    state: Start
        q!: $regex</start>
        script:sleep(1500); $dialer.setNoInputTimeout(1000); 
            
        go!: /Start/NewStart
        state:NewStart || modal = true
            q:да
            q:*
            script:
                $dialer.setNoInputTimeout(2000);
                $dialer.setTtsConfig({emotion: "good"});
                $temp.Check = {}
                if($session.Repeat_Row>=1){{}}
                else{$session.Repeat_Row=0;}
                getPayLoad()
                                
                $session.new = 'Yes'
                $session.second_state = 0
                $session.third_state = 0 
            if:$session.Repeat_Row == 0
                a: Здравствуйте.|| bargeInIf = beforeHangup, bargeInTransition = /StartKZ , bargeInLabel = firstReply
                a: Я виртуальный помощник компании алсеко. || bargeInIf = beforeHangup,  bargeInTransition = /StartKZ, bargeInLabel = SecondReply
                a: Kоторая ежемесячно формирует и печатает счета на оплату коммунальных и иных услуг. || bargeInIf = beforeHangup,  bargeInTransition = /StartKZ, bargeInLabel = ThirdReply
                a: Подскажите, был ли вам доставлен бумажный счет за {{getMonthName($jsapi.dateForZone("Europe/Moscow", "MM"))}}? || bargeInIf = beforeHangup,  bargeInTransition = /StartKZ , bargeInLabel = FourthReply
            else
                a: Подскажите, был ли вам доставлен бумажный счет за {{getMonthName($jsapi.dateForZone("Europe/Moscow", "MM"))}}?|| bargeInIf = beforeHangup,  bargeInTransition = /StartKZ, bargeInLabel = fifthReply
            script:
                $dialer.bargeInResponse({
                bargeIn: "forced",
                bargeInTrigger: "final",
                noInterruptTime: 0});  
            # a:  {{toPrettyString($dialer.getPayload())}}  
            state: ToKazakh
                intent:/Казахский_язык
                go!:/StartKZ
            state: BargeInIntent || noContext = true
                event: bargeInIntent
                script:
                    var bargeInIntentStatus = $dialer.getBargeInIntentStatus();
                    log(bargeInIntentStatus.bargeInIf); // => "beforeHangup"
                    var text = bargeInIntentStatus.text;
                    for (var i = 0; i < patterns_words.length; i++) {
                        if (text.indexOf(patterns_words[i]) > -1) {
                            $dialer.bargeInInterrupt(true);
                            break;
                        }
                    }

      

            state: IDontKnowHere
                script:$dialer.setNoInputTimeout(3000);
                    
                intent: /НеЗнаю
                go!: /Start/NewStart/NoMatch
            state: YesMyNumber || modal = true
                intent: /Согласие
                q: $yes *
                q: $agree *
                script:$dialer.setNoInputTimeout(3000);
                    $session.second_state = 'Получил'
                    $analytics.setSessionData("Результат", 'Получил');
                    $session.Repeat_Row=+1
                a: Можете уточнить дату получения?
                state: DateWasAsked
                   
                    intent: /Дата
                    script:$dialer.setNoInputTimeout(1500);
                         $analytics.setSessionData("Дата Получения Квитанции", $request.query );
                         $session.third_state = $request.query
                    
                    go!: /Start/NewStart/FinishDialogs  
                        
                state: IDontKnow
                    intent: /НеЗнаю
                    intent: /НеСогласие
                    q: $no * 
                    q: $disagree *
                    event: noMatch
                    event: match 
                    script:$dialer.setNoInputTimeout(1500);
                         $analytics.setSessionData("Дата", "Забыл");
                         $session.third_state = "Не помню"
                    go!:/Start/NewStart/FinishDialogs     
    
    
            state: NoMyNumber || modal = true
                intent: /НеСогласие
                q: $no * 
                q: $disagree *
                script:$dialer.setNoInputTimeout(3000);
                    $session.second_state = 'Не Получил'
                    $session.Repeat_Row = $session.Repeat_Row+1
                   $analytics.setSessionData("Результат", 'Не Получил');
                a: Подскажите, когда в последний раз приходила квитанция?
    
                state: DateWasAsked
                    
                    intent: /Дата
                    script:$dialer.setNoInputTimeout(1500);
                        $analytics.setSessionData("Дата", $request.query );
                        $session.third_state = $request.query
                    
                    go!: /Start/NewStart/FinishDialogs
                    
                state: IDontKnow
                    intent: /НеЗнаю
                    intent: /НеСогласие
                    q: $no * 
                    q: $disagree *
                    event: noMatch    
                    event: match   
                    script:$dialer.setNoInputTimeout(1500);
                        $analytics.setSessionData("Дата", "Забыл");
                        $session.third_state ="Не помню"
                    
                    go!: /Start/NewStart/FinishDialogs
            state: NoMatch || noContext = true
                event: noMatch
                if:  $session.Repeat_Row==0
                    script:$dialer.setNoInputTimeout(1000);
                        $session.Repeat_Row = $session.Repeat_Row+1 
                    a: Извините, я не расслышала || bargeInTransition = /StartKZ, bargeInLabel = firstReply
                    go!: /Start/NewStart
                else
                    script:$dialer.setNoInputTimeout(1000);
                        $analytics.setSessionData("Дата", "Ошибка" );
                    
                    go!: /Start/NewStart/FinishDialogs
        
            state: FinishDialogs
                script:$dialer.setNoInputTimeout(3000);
                    if ((toPrettyString($context.session.contextPath).indexOf('NoMyNumber')  >-1)|| (toPrettyString($context.session.contextPath).indexOf('NoMatch')>-1))
                        $temp.Check = true;
                    else
                        $temp.Check = false;
                if: $temp.Check ===true
                    random:
                        a: Спасибо за обратную связь, уточним причину недоставки квитанции.
                        a: Спасибо за обратную связь, выясним причину отсутствия квитанции.                   
                    
                else
                    a: Спасибо за обратную связь! До свидания! 
                    

                script:
                    $dialer.hangUp();
                go!: /Start/NewStart/SaveData
            state: SaveData
                event!: hangup
                script:
                    var values = JSON.parse(Integration_Test_Google()).values
                    $temp.temp = 0
                    $temp.row = 0
                    $temp.count=0
                    for(var i = 1 ; i < values.length; i ++){ 
                            if  (values[i] == $request.userFrom.firstName){
                                $temp.temp = 1
                                $temp.row = i
                                break
                            }
                        
                        } 
                    if($temp.temp ==1){
                        $temp.count = Return_Count($temp.row)
                    }
                
                if: $temp.temp ==1
                    GoogleSheets:
                        operationType = clearCellData
                        integrationId = 8fc0fbcd-3f4e-40e9-bcd6-afe264acebc4
                        spreadsheetId = 1mKKaRoY--a76CPJv7jAhefcQWI5KbdrPWHuHbD5Nqbc
                        sheetName = IntegrationList
                        body = {"values":["A{{$temp.row+1}}:O{{$temp.row+1}}"]}
                    GoogleSheets:
                        operationType = writeDataToLine
                        integrationId = 8fc0fbcd-3f4e-40e9-bcd6-afe264acebc4
                        spreadsheetId = 1mKKaRoY--a76CPJv7jAhefcQWI5KbdrPWHuHbD5Nqbc
                        sheetName = IntegrationList
                        body = {"values": [ "{{$dialer.getPayload().лицевой_счет}}","{{$dialer.getPayload().населенный_пункт}}", "{{$dialer.getPayload().тип_улицы_микрорайона}}",  "{{$dialer.getPayload().улица}}","{{$dialer.getPayload().дом}}", "{{$dialer.getPayload().квартира}}", "{{$dialer.getPayload().номер_телефона}}","{{$dialer.getPayload().реестр_печати_по_последней_печати}}","{{$dialer.getPayload().способ_доставки}}","{{$session.new}}","{{$temp.count}}","{{currentDate().locale("ru").format("dddd, MMMM Do YYYY, h:mm:ss a")}}","{{$session.second_state}}","{{$session.third_state}}",{{toPrettyString($request.userFrom.firstName)}}]}
                else
                    GoogleSheets:
                        operationType = writeDataToLine
                        integrationId = 8fc0fbcd-3f4e-40e9-bcd6-afe264acebc4
                        spreadsheetId = 1mKKaRoY--a76CPJv7jAhefcQWI5KbdrPWHuHbD5Nqbc
                        sheetName = IntegrationList
                        body = {"values":  [   "{{$dialer.getPayload().лицевой_счет}}","{{$dialer.getPayload().населенный_пункт}}", "{{$dialer.getPayload().тип_улицы_микрорайона}}",  "{{$dialer.getPayload().улица}}","{{$dialer.getPayload().дом}}", "{{$dialer.getPayload().квартира}}", "{{$dialer.getPayload().номер_телефона}}","{{$dialer.getPayload().реестр_печати_по_последней_печати}}","{{$dialer.getPayload().способ_доставки}}","{{$session.new}}","1","{{currentDate().locale("ru").format("dddd, MMMM Do YYYY, h:mm:ss a")}}","{{$session.second_state}}","{{$session.third_state}}",{{toPrettyString($request.userFrom.firstName)}}]}

            state: speechNotRecognized
                event: speechNotRecognized
                script:
                    $session.speechNotRecognized = $session.speechNotRecognized || {};
                    //Начинаем считать попадания в кэчол с нуля, когда предыдущий стейт не кэчол.
                    if ($session.lastState && !$session.lastState.startsWith("/speechNotRecognized")   &&  !$session.lastState.startsWith('/Start/NewStart/speechNotRecognized')) {
                        $session.speechNotRecognized.repetition = 0;
                    } else{
                        $session.speechNotRecognized.repetition = $session.speechNotRecognized.repetition || 0;
                    }
                    $session.speechNotRecognized.repetition += 1;
                if: $session.speechNotRecognized.repetition >= 2
                    a: Кажется, проблемы со связью. Мне Вас не слышно.
                    script:
                        $dialer.hangUp();
                        $analytics.setSessionResult("Не слышно")
                         
                else:
                    random: 
                        a: Извините, я не расслышала. Повторите, пожалуйста.
                        a: Не совсем поняла. Можете повторить, пожалуйста?
                        a: Повторите, пожалуйста. Вас не слышно.
        # state: Match
        #     event!: match
        #     a: {{$context.intent.answer}}
        

    
    #Казахский Язык
    state: StartKZ
        intent!:/Казахский_язык
        script:
            $dialer.setTtsConfig({ "lang": "kk-KK", "voice": "amira", "speed": 1.4, 'useV3': true});
            $dialer.setNoInputTimeout(3000);
        
            $temp.Check = {}
            if($session.Repeat_RowKZ>=1){{}}
            else{$session.Repeat_RowKZ=0;}
            if($dialer.getPayload().Имя)
                $analytics.setSessionData("Имя", $dialer.getPayload().Имя );
            if($dialer.getPayload().Фамилия)
                $analytics.setSessionData("Фамилия", $dialer.getPayload().Фамилия);
            if($dialer.getPayload().Месяц)
                $analytics.setSessionData("Месяц", $dialer.getPayload().Месяц);
            $session.new = 'Yes'
            $session.second_state = 0
            $session.third_state = 0 
        if:$session.Repeat_RowKZ == 0
            # a: Сәлеметсіз бе! Мен Алсеко компаниясының виртуалды көмекшісімін.
            # # a: Бізайсайын коммунал+дық және басқа қызметтер+ді төлеугеарналған шоттар+дықалыптасты+рып, басып шығарамыз.
            ## a: Бізайсайын коммуналдық және басқа қызметтерді төлеугеарналған шоттардықалыптастырып, басып шығарамыз.    
            # a: Сізге {{getMonthNameKZ($jsapi.dateForZone("Europe/Moscow", "MM"))}} айының электронды шоты жеткізілді ме?
            
            a: Сәлеметсіз бе! Мен ай сайын коммуналдық және басқа да қызметтерді төлеуге арналған шоттарды қалыптастырып, басып шығаратын алсеко компаниясының виртуалдық көмекшісімін. 
            a: Айтыңызшы, Сізге {{getMonthNameKZ($jsapi.dateForZone("Europe/Moscow", "MM"))}} айының қағаз шоты жеткізілді ме?
            
            
            
            
        else
            a: Сізге {{getMonthNameKZ($jsapi.dateForZone("Europe/Moscow", "MM"))}} айының электронды шоты жеткізілді ме?
        state: IDontKnowHereKZ
            script:$dialer.setNoInputTimeout(3000);
            intent: /НеЗнаю
            go!: /NoMatchKZ
        state: YesMyNumberKZ || modal = true
            intent: /Согласие
            q: $yes *
            q: $agree *
            script:$dialer.setNoInputTimeout(3000);
                $session.second_state = 'Да'
                $analytics.setSessionData("Наличие Квитанции", "Да")
                $session.Repeat_RowKZ=+1
            a: Шот қай күні келді?
            state: DateWasAskedKZ
                intent: /Дата
                script:$dialer.setNoInputTimeout(1500);
                     $analytics.setSessionData("Дата Получения Квитанции", $request.query );
                     $session.third_state = $request.query
                go!: /StartKZ/FinishDialogsKZ 
                    
            state: IDontKnowKZ
                intent: /НеЗнаю
                intent: /НеСогласие
                q: $no * 
                q: $disagree *
                event: match 
                event: noMatch    
                script:$dialer.setNoInputTimeout(1500);
                     $analytics.setSessionData("Дата Получения Квитанции", "Не помню");
                     $session.third_state = "Не помню"
                go!:/StartKZ/FinishDialogsKZ    


        state: NoMyNumberKZ || modal = true
            intent: /НеСогласие
            q: $no * 
            q: $disagree *
            script:$dialer.setNoInputTimeout(3000);
                $session.second_state = 'Нет'
                $session.Repeat_RowKZ = $session.Repeat_RowKZ+1
                $analytics.setSessionData("Наличие Квитанции", "Нет")
            a: Шот соңғы рет қашан келді?

            state: DateWasAskedKZ
                intent: /Дата
                script:$dialer.setNoInputTimeout(1500);
                    $analytics.setSessionData("Дата Получения Квитанции", $request.query );
                    $session.third_state = $request.query
                    
                go!: /StartKZ/FinishDialogsKZ
            state: IDontKnowKZ
                intent: /НеЗнаю
                intent: /НеСогласие
                q: $no * 
                q: $disagree *
                event: match   
                event: noMatch 
                script:$dialer.setNoInputTimeout(1500);
                     $analytics.setSessionData("Дата Получения Квитанции", "Не помню");
                     $session.third_state = "Не помню"
                go!: /StartKZ/FinishDialogsKZ          
                
        state: NoMatchKZ || noContext = true
            
            event: noMatch
            if:  $session.Repeat_RowKZ==0
                script:$dialer.setNoInputTimeout(1000); $session.Repeat_RowKZ = $session.Repeat_RowKZ+1; 
                   
                a: Кешіріңіз, айтқаныңыз дұрыс естілмеді
                go!: /StartKZ
            else
                script:$dialer.setNoInputTimeout(1000);
                    $analytics.setSessionData("Дата Получения Квитанции", "Не разборчиво" );
                
                go!: /StartKZ/FinishDialogsKZ  

        state: FinishDialogsKZ
            script:$dialer.setNoInputTimeout(2000);
                if ((toPrettyString($context.session.contextPath).indexOf('NoMyNumberKZ')  >-1)|| (toPrettyString($context.session.contextPath).indexOf('NoMatch')>-1))
                    $temp.Check = true;
                else
                    $temp.Check = false;
            if: $temp.Check==true
                 random:
                    a:Жауабыңызға рахмет. Шоттың келмеген себебін нақтылаймыз.
                
            else
                a:Жауабыңыз үшін рахмет! Сау болыңыз!
                    # a: Спасибо за обратную связь, выясним причину отсутствия квитанции.
            script:
                $dialer.hangUp();
            go!: /StartKZ/SaveDataKZ
        state: SaveDataKZ
            event!: hangup
            script:
                var values = JSON.parse(Integration_Test_Google()).values
                $temp.temp = 0
                $temp.row = 0
                $temp.count=0
                for(var i = 1 ; i < values.length; i ++){ 
                        if  (values[i] == $request.userFrom.firstName){
                            $temp.temp = 1
                            $temp.row = i
                            break
                        }
                    
                    } 
                if($temp.temp ==1){
                    $temp.count = Return_Count($temp.row)
                }
    
            if: $temp.temp ==1
                GoogleSheets:
                    operationType = clearCellData
                    integrationId = 8fc0fbcd-3f4e-40e9-bcd6-afe264acebc4
                    spreadsheetId = 1mKKaRoY--a76CPJv7jAhefcQWI5KbdrPWHuHbD5Nqbc
                    sheetName = IntegrationList
                    body = {"values":["A{{$temp.row+1}}:O{{$temp.row+1}}"]}
                GoogleSheets:
                    operationType = writeDataToLine
                    integrationId = 8fc0fbcd-3f4e-40e9-bcd6-afe264acebc4
                    spreadsheetId = 1mKKaRoY--a76CPJv7jAhefcQWI5KbdrPWHuHbD5Nqbc
                    sheetName = IntegrationList
                    body = {"values": [ "{{$dialer.getPayload().лицевой_счет}}","{{$dialer.getPayload().населенный_пункт}}", "{{$dialer.getPayload().тип_улицы_микрорайона}}",  "{{$dialer.getPayload().улица}}","{{$dialer.getPayload().дом}}", "{{$dialer.getPayload().квартира}}", "{{$dialer.getPayload().номер_телефона}}","{{$dialer.getPayload().реестр_печати_по_последней_печати}}","{{$dialer.getPayload().способ_доставки}}","{{$session.new}}","{{$temp.count}}","{{$session.third_state}}","{{$session.second_state}}","{{currentDate().locale("ru").format("dddd, MMMM Do YYYY, h:mm:ss a")}}",{{toPrettyString($request.userFrom.firstName)}}]}
            else
                GoogleSheets:
                    operationType = writeDataToLine
                    integrationId = 8fc0fbcd-3f4e-40e9-bcd6-afe264acebc4
                    spreadsheetId = 1mKKaRoY--a76CPJv7jAhefcQWI5KbdrPWHuHbD5Nqbc
                    sheetName = IntegrationList
                    body = {"values":  [   "{{$dialer.getPayload().лицевой_счет}}","{{$dialer.getPayload().населенный_пункт}}", "{{$dialer.getPayload().тип_улицы_микрорайона}}",  "{{$dialer.getPayload().улица}}","{{$dialer.getPayload().дом}}", "{{$dialer.getPayload().квартира}}", "{{$dialer.getPayload().номер_телефона}}","{{$dialer.getPayload().реестр_печати_по_последней_печати}}","{{$dialer.getPayload().способ_доставки}}","{{$session.new}}","1","{{currentDate().locale("ru").format("dddd, MMMM Do YYYY, h:mm:ss a")}}","{{$session.second_state}}","{{$session.third_state}}",{{toPrettyString($request.userFrom.firstName)}}]}

        state: speechNotRecognizedGlobalKZ
            event: speechNotRecognized
            script:
                $session.speechNotRecognized = $session.speechNotRecognized || {};
                //Начинаем считать попадания в кэчол с нуля, когда предыдущий стейт не кэчол.
                if ($session.lastState && !$session.lastState.startsWith("/speechNotRecognizedGlobalKZ") && !$session.lastState.startsWith('/StartKZ/speechNotRecognizedGlobalKZ')) {
                    $session.speechNotRecognized.repetition = 0;
                } else{
                    $session.speechNotRecognized.repetition = $session.speechNotRecognized.repetition || 0;
                }
                $session.speechNotRecognized.repetition += 1;

            if: $session.speechNotRecognized.repetition > 1
                a: Байланыс нашар сияқты, дауысыңыз естілмейді
                script:
                    $dialer.hangUp();
                    $analytics.setSessionResult("Не слышно")
            else:
                random: 
                    a: Кешіріңіз, естілмей қалды. Қайталап жіберіңізші?
                    a: Айтқаныңызды түсінбей қалдым. Қайталап өтіңізші?
                    a: Айтқаныңыз естілмеді, қайталап жіберуіңізді өтінемін
        # state: Match
        #     event!: match
        #     a: {{$context.intent.answer}}
    # state: speechNotRecognizedGlobal
    #     event!: speechNotRecognized
    #     script:
    #         $session.speechNotRecognized = $session.speechNotRecognized || {};
    #         //Начинаем считать попадания в кэчол с нуля, когда предыдущий стейт не кэчол.
    #         if ($session.lastState && !$session.lastState.startsWith("/speechNotRecognizedGlobal") ) {
    #             $session.speechNotRecognized.repetition = 0;
    #         } else{
    #             $session.speechNotRecognized.repetition = $session.speechNotRecognized.repetition || 0;
    #         }
    #         $session.speechNotRecognized.repetition += 1;
    #     if: $session.lastState.startsWith("/Start")   
    #         script: $session.timeout = {};
    #         go!: /Start
    #     else   
    #         if: $session.speechNotRecognized.repetition > 1
    #             a: Кажется, проблемы со связью. Мне Вас не слышно.
    #             script:
    #                 $dialer.hangUp();
    #                 $analytics.setSessionResult("Не слышно")
    #         else:
    #             random: 
    #                 a: Извините, я не расслышала. Повторите, пожалуйста.
    #                 a: Не совсем поняла. Можете повторить, пожалуйста?
    #                 a: Повторите, пожалуйста. Вас не слышно.  
        