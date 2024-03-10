import http from 'k6/http';
import { check,sleep } from 'k6';

export default function() {

    const queryOnly = false;
    const numConsurrentSearches = 7;
    const deleteAll = false;


    // derive a random # between 0 and 50, if it's < 3, put a record
    const putTransactionFreq=Math.floor(Math.random()*50);
    const putTransaction= (putTransactionFreq <= 3);

    const url = 'https://where-for-dinner-search.apps.h2o-2-22522.h2o.vmware.com/search';
    if(putTransaction){
        // derive a sleep interval from 0 to .5 second
        const sleepInterval=Math.floor(Math.random()*50)*10;
        const putTransactionFreq=Math.floor(Math.random*50);
        const sleepIntervalInSecs = sleepInterval / 1000;
        //console.log ('sleeping '+sleepIntervalInSecs );
        sleep(sleepInterval);
        const searchName="test"+new Date();
        //console.log('searchName: ' +searchName);

        const payload = JSON.stringify({
            name: searchName,
            postalCode: 64106,
            radius: 50,
            diningNames: "Bristol",
            startTime: 1713295320000,
            endTime: 1713514920000,
            continousSearch: true,
            sendResultsTo: ""
        });
        const params = {
            timeout: '120s',
            headers: {
                'Content-Type': 'application/json',
            }
        };
        if (!queryOnly){
            const res=http.put(url, payload, params);
            if(res.status!=200 && res.status !=201 && res.status!=409)console.log ('put status '+res.status );
            check(res, {
                'status was 200': (r) => r.status === 200 || r.status === 201 || r.status === 409,
            });
            sleep(10);
        }
    }

    // now get any older requests and delete the first in the list if there are more than the allowable consurrent searches or if deleteAll (lowest id)
    const qres = http.get('https://where-for-dinner-search.apps.h2o-2-22522.h2o.vmware.com/search');
    if(qres.status !=200 && qres.status !=201 && qres.status!=409) console.log ( 'get status '+qres.status );
    check(qres, {
        'status was 200': (r) => r.status === 200,
    });
    if (qres!=null && qres.status==200 )  {
        const qres_json=qres.json();
        if (qres_json!=null && qres_json.length > 0){

            if(deleteAll || qres_json.length >= numConsurrentSearches  ){
                const id=qres_json[0].id;
                // delete using the id of the earliest previous search
                const delete_url='https://where-for-dinner-search.apps.h2o-2-22522.h2o.vmware.com/search/'+id;
                //  console.log(delete_url);
                const dres=http.del(delete_url,null, null);
                if(dres.status !=200)console.log('delete status '+ dres.status);
                check(dres, {
                    'status was 200': (r) => r.status === 200,
                });
            }
        }
    }
}
