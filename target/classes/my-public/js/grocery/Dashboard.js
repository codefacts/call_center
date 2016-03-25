site.reactjs.Dashboard = React.createClass({
    getDefaultProps: function () {
        return {
            onInit: function () {
            },
            contactsCountByDateUrl: "/contacts/countByDate",
            contactsSummaryUrl: "/contacts/summary",
        }
    },
    getInitialState: function () {
        var $this = this;
        return {
            contactsCountByDate: [],
            contactsSummary: {}
        };
    },
    componentDidMount: function () {
        var $this = this;
        $this.props.onInit($this);
        $this.getDataCountByDate();
        $this.getContactsSummary();

    },
    render: function () {
        var $this = this;

        var char = {
            data: $this.state.contactsCountByDate.map(function (data) {
                return {
                    'dataCount': data.dataCount,
                    'date': new Date(data.date).getDate()
                };
            }),
            ykeys: ['dataCount'],
            xkey: 'date',
            labels: ['Data'],
            hideHover: 'auto',
            resize: true
        }

        return (

            <div className="row">
                <div className="col-lg-6 col-md-6">
                    <div className="panel panel-primary">
                        <div className="panel-heading">
                            <div className="row">
                                <div className="col-xs-3">
                                    <i className="fa fa-comments fa-5x"></i>
                                </div>
                                <div className="col-xs-9 text-right">
                                    <div className="huge">{$this.state.contactsSummary.totalCount}</div>
                                    <div>Total Data</div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
                <div className="col-lg-6 col-md-6">
                    <div className="panel panel-green">
                        <div className="panel-heading">
                            <div className="row">
                                <div className="col-xs-3">
                                    <i className="fa fa-tasks fa-5x"></i>
                                </div>
                                <div className="col-xs-9 text-right">
                                    <div className="huge">{$this.state.contactsSummary.todayCount}</div>
                                    <div>Today</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>


                <div className="col-md-12">
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            Data Received by date
                        </div>
                        <div className="panel-body">
                            {!!char.data.length ?
                                <site.reactjs.MorrisBar id="morris-bar-chart" style={{height: '450px', width: '100px'}}
                                                        barConfig={char}/> : ""}
                        </div>
                    </div>
                </div>


            </div>

        );
    },
    updateData: function (params) {
        console.log("updated")
        console.log(params)
    },

    getDataCountByDate: function () {
        var $this = this;
        $.ajax({
            url: $this.props.contactsCountByDateUrl,
            cache: false,
            success: function (data) {
                $this.setState({
                    contactsCountByDate: data
                });
            },
            error: function () {
                alert("Server error. Error Code: INFO");
            }
        })
    },

    getContactsSummary: function () {
        var $this = this;
        $.ajax({
            url: $this.props.contactsSummaryUrl,
            cache: false,
            success: function (data) {
                $this.setState({
                    contactsSummary: data
                });
            },
            error: function () {
                alert("Server error. Error Code: INFO");
            }
        })
    },
});