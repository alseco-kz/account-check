init:
    bind("preProcess", function($context) {
        $context.session._lastState = $context.currentState;
        if ($context.session.speedChanged){
            $dialer.setTtsConfig({speed: 1.1});
            $context.session.speedChanged = false;
        }
        //$context.session._lastState = $context.contextPath ;
    });
    bind("preMatch", function($context) {
        if($context.request.query){
            $context.request.query = $context.request.query.replaceAll("два нуля","ноль ноль")
            $context.request.query = $context.request.query.replaceAll("два ноля","ноль ноль")
            $context.request.query = $context.request.query.replaceAll("три ноля","ноль ноль ноль")
            $context.request.query = $context.request.query.replaceAll("три нуля","ноль ноль ноль")
            
        }
        //$context.request.query += " (клиент авторизован)";
    });
    
    bind("postProcess", function($context) {
        // предыдущий стейт с учетом неконтекстных
        $context.session.lastState = $context.currentState;
        // предыдущий стейт без учета неконтестных
        $context.session.prevState = $context.contextPath;
        
        // $analytics.setComment(toPrettyString($dialer.getTtsConfig()))
        //$context.session._lastState = $context.currentState;
        // log("**********" + toPrettyString($context.currentState));
        $context.session.AnswerCnt = $context.session.AnswerCnt || 0;
        if ((!$context.session.lastState.startsWith("/speechNotRecognizedGlobal"))
            && (!$context.session.lastState.startsWith("/Start/DialogMakeQuestion"))
           )
            $context.session.AnswerCnt += 1;
        
        //$context.session._lastState = $context.contextPath ;
        // добавляю логи всех ответов бота
        $context.session._last_reply = "";
        if ($context.response.replies) {
            var last_reply = "";
            $context.response.replies.forEach(function(reply) {
                if (reply.type === "text") {
                    if (reply.text.match(/\[|\]/g) && reply.text.match(/\(|\)/g)) {
                        last_reply += formatLink(reply.text)
                    } else {
                        last_reply += reply.text
                    }
                }
            });
            $context.session._last_reply =  last_reply;
        }        
    });
    // ///ChangeAccountPerson/ChangeAccountPerson
    // bind("selectNLUResult", 
    // function($context) {
        
    //     // log("$context 1 = "  + toPrettyString( $context ) );
    //     log("$context.nluResults 1 = "  + toPrettyString( $context.nluResults) );
        
    //     // Для блока ввод ЛС - когда вводим цифры не применять приоритет интетов над паттернами.
    //     // ошибка происходит, если говоришь - "четыре" (синоним хорошо) или "пять" (синоним отлично)
    //     if (
    //         $context.contextPath && 
    //         $context.contextPath.startsWith("/BlockAccountNumInput/AccountInput/AccountInputNumber") &&
    //         ($context.nluResults.selected.clazz == "/BlockAccountNumInput/AccountInput/AccountInputNumber/AccountInputNumberContinue")
    //         )
    //     {
    //         return;
    //     }
    //     // для начала диалога 
    //     // если что-то прописано в паттерне, то оставляем приоритет за ним 
    //     // требования к паттернам - только нужные слова, без всяких звездочек и т.п. 
    //     if (
    //         $context.contextPath && 
    //         $context.contextPath.startsWith("/Start") &&
    //         ($context.nluResults.selected.ruleType == "pattern") &&
    //         ($context.nluResults.selected.clazz == "/Start/DialogMakeQuestion")
    //         )
    //     {
    //         return;
    //     }
    //     if (
    //         $context.contextPath && 
    //         ($context.nluResults.selected.ruleType == "pattern") &&
    //         ($context.nluResults.selected.pattern == "$numbersByWords")
    //         )
    //     {
    //         return;
    //     }        
        
    //     if (
    //         $context.contextPath && 
    //         ($context.nluResults.selected.ruleType == "pattern") &&
    //         ($context.nluResults.selected.clazz == "/Taxes/TaxQuestion")
    //         )
    //     {
    //         return;
    //     }        
    //     // # log("step2");
    //     // если состояние по "clazz":"/NoMatch" - то оставляем приоритет 
    //     if (
    //             ($context.nluResults.intents.length > 0) && 
    //             ($context.nluResults.intents[0].score > 0.35) && 
    //             $context.nluResults.intents[0].clazz &&
    //             ($context.nluResults.intents[0].clazz != "/NoMatch")
    //         ) {
    //             // если правило - паттерн и приводит к интенту /SupplierContacts/SupplierContacts, то не меняем
    //         if (!($context.nluResults.selected.clazz && 
    //             (
    //                 ($context.nluResults.selected.clazz.startsWith("/SupplierContacts/SupplierContacts"))
    //                 && !($context.nluResults.selected.clazz.startsWith("/SupplierContacts/SupplierContacts/CanIHelpYou"))
    //             )
    //             )
    //             ){
    //                 // log("ChangeToIntent1");
    //                 $context.nluResults.selected = $context.nluResults.intents[0];
    //         }
            
    //         // log("$context.nluResults.selected"  + toPrettyString( $context.nluResults.selected) );
            
    // }
    bind("selectNLUResult", function($context) {
    // Получим все результаты от всех классификаторов в виде массива.
        var allResults = _.chain($context.nluResults)
            .omit("selected")
            .values()
            .flatten()
            .value();
        
        // Сосчитаем максимальное значение `score` среди всех результатов.
        var maxScore = _.chain(allResults)
            .pluck("score")
            .max()
            .value();
        
        // Запишем в `nluResults.selected` результат с максимальным весом.
        $context.nluResults.selected = _.findWhere(allResults, {
            score: maxScore
     });
        log($context.nluResults);
        
        if ($context.nluResults.intents.length > 0) {
            // Если есть хотя бы один результат от классификатора на интентах, используем первый результат.
            $context.nluResults.selected = $context.nluResults.intents[0];
            return;
        }
        
        if ($context.nluResults.patterns.length > 0) {
            // Если результата от интентов нет, используем результат от паттернов.
            $context.nluResults.selected = $context.nluResults.patterns[0];
    }
    });  
    


        // # log("step2");

        // обработка фразы "да нужна повтори помедленней я записываю

    // # bind("selectNLUResult", function($context) {
    // #     // Получим все результаты от всех классификаторов в виде массива.
    // #     var allResults = _.chain($context.nluResults)
    // #         .omit("selected")
    // #         .values()
    // #         .flatten()
    // #         .value();
    
    // #     // Сосчитаем максимальное значение `score` среди всех результатов.
    // #     var maxScore = _.chain(allResults)
    // #         .pluck("score")
    // #         .max()
    // #         .value();
    
    // #     // Запишем в `nluResults.selected` результат с максимальным весом.
    // #     $context.nluResults.selected = _.findWhere(allResults, {
    // #         score: maxScore
    // #     });
    // #     log(toPrettyString($context.nluResults.selected));
    // # });
    

    $global.mainSupplConverter = function($parseTree){
        var id = $parseTree.MainSuppl[0].value;
        return $MainSuppl[id].value;
    }