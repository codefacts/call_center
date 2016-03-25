site.reactjs.AppUm = React.createClass({
    getInitialState: function () {
        return {
            body: "",
            bodyRef: null,
            user: {},
            campaign: {}
        };
    },

    componentDidMount: function () {
        var $this = this;
        $this.router();
        $this.getUserData();
        $this.getCampaign();

        eb.send('FIND_ALL_CALL_OPERATOR', {}, {}, function (err, msg) {
            if (!!err) {
                alert("Error in server. Please reload the page.");
                return;
            }
            msg.body = msg.body || [];
            window.site.CALL_OPERATORS = msg.body.reduce(function (map, op) {
                map[op.CALL_OPERATOR_ID] = op;
                return map;
            }, {});
        });
    },

    render: function () {
        var $this = this;
        var user = $this.state.user;
        var campaign = $this.state.campaign;
        return (
            <div className="container-fluid">

                <div className="row">
                    <div className="col-md-12">
                        <site.reactjs.NavbarPrimary user={user} campaign={campaign}/>
                    </div>
                </div>

                {$this.state.body}

            </div>
        );
    },
    router: function () {
        var $this = this;
        site.hash
            .on('/', function () {
                $this.setState({
                    body: <site.reactjs.Step1 onInit={$this.onInitBody}/>
                }, function () {
                    $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .on('/work-day-details', function () {
                $this.setState({
                    body: <site.reactjs.Step2 user={$this.state.user} onInit={$this.onInitBody}/>
                }, function () {
                    $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .on('/call', function () {
                $this.setState({
                    body: <site.reactjs.ContactDetailsAndCall onInit={$this.onInitBody}/>
                }, function () {
                    $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .on('/grocery-form', function () {
                $this.setState({
                    body: <site.reactjs.grocery.Step1 onInit={$this.onInitBody}/>
                }, function () {
                    console.log("updating contacts: " + $this.bodyRef.updateData);
                    if ($.isFunction($this.bodyRef.updateData)) $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .execute()
        ;
    },
    onInitBody: function (comp) {
        this.bodyRef = comp;
    },
    getUserData: function () {
        var $this = this;
        $.ajax({
            url: '/current-user',
            cache: false,
            success: function (user) {
                console.log(user)
                window.currentUser = user;
                $this.setState({
                    user: user
                });
            },
            error: function () {
            }
        });
    },
    getCampaign: function () {
        var $this = this;
        $.ajax({
            url: '/current-campaign',
            cache: false,
            success: function (campaign) {

                $this.setState({
                    campaign: campaign
                });
            },
            error: function () {
            }
        });
    }
});

if (eb.state == EventBus.CLOSED) {
    alert("Please reload the page. You are now disconnected from the server.");
}

document.addEventListener('EVENT_BUS_DISCONNECTED', function () {
    alert("Please reload the page. You are now disconnected from the server.");
});

if (eb.state === 1) {
    registerEventBusHandlers();
} else {
    document.addEventListener('EVENT_BUS_CONNECTED', function () {

        registerEventBusHandlers();

    });
}


function registerEventBusHandlers() {
    console.log("EB registering ALREADY_LOCKED");
    eb.registerHandler('ALREADY_LOCKED', null, function (err, msg) {
        console.log("already_locked");
        var params = site.hash.params();
        var call_operator = parseInt(params.call_operator);
        var sms_id = parseInt(params.sms_id);

        if ((msg.body.SMS_ID == sms_id) && (msg.body.LOCKED_BY !== call_operator)) {
            alert("An agent is already calling on this sms_id: " + sms_id + ". " +
                "Please choose another contact.");
        }
    });
}