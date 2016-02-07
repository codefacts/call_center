site.reactjs.App = React.createClass({
    getInitialState: function () {
        return {
            body: "",
            bodyRef: null
        };
    },
    componentDidMount: function () {
        var $this = this;
        $this.router();
    },

    render: function () {
        var $this = this;

        return (
            <div className="container-fluid">

                <div className="row">
                    <div className="col-md-12">
                        <site.reactjs.NavbarPrimary />
                    </div>
                </div>

                {$this.state.body}

            </div>
        );
    },
    router: function () {
        var $this = this;
        site.hash
            .on('/', function () {      //Dashboard
                $this.setState({
                    body: <site.reactjs.Dashboard onInit={$this.onInitBody}/>
                }, function () {
                    console.log("updating dashboard: " + $this.bodyRef.updateData);
                    if ($.isFunction($this.bodyRef.updateData)) $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .on('/contacts', function () {      //Contacts
                $this.setState({
                    body: <site.reactjs.Step1 onInit={$this.onInitBody}/>
                }, function () {
                    console.log("updating contacts: " + $this.bodyRef.updateData);
                    if ($.isFunction($this.bodyRef.updateData)) $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .on('/summary', function () {      //Summary
                $this.setState({
                    body: <site.reactjs.Step2 onInit={$this.onInitBody}/>
                }, function () {
                    console.log("updating contacts: " + $this.bodyRef.updateData);
                    if ($.isFunction($this.bodyRef.updateData)) $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .on('/summary-details', function () {      //Summary
                $this.setState({
                    body: <site.reactjs.Step3 onInit={$this.onInitBody}/>
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
    }
});