site.reactjs.TablePrimary = React.createClass({

    getDefaultProps: function () {
        return {
            data: [],
            onInit: function () {
            }
        };
    },
    getInitialState: function () {
        return this.interceptState({
            data: this.props.data,
            __render: true
        });
    },
    componentDidMount: function () {
        var $this = this;
        $this.props.onInit($this);
    },
    componentWillUnmount: function () {
        var $this = this;
    },
    onWindowResize: function () {
        var $this = this;
    },
    shouldComponentUpdate: function (nextProps, nextState) {
        return this.state.__render !== nextState.__render;
    },
    render: function () {
        console.log("render: TablePrimary")
        var $this = this;
        var data = $this.state.data;
        return (
            <div className="table-responsive TablePrimary"
                 style={{border: '1px solid #ddd', height: $this.state.bodyHeight + 'px'}}>

                <table className="table table-bordered table-hover table-condensed MainTable"
                       style={{marginTop: '37px', height: $this.tableHeight($this.state.bodyHeight) + 'px', overflow: 'auto',
                       display:'block', border: 0, borderTop: '1px solid #ddd'}}>

                    <thead style={{display: 'block', position: 'absolute', top: '0', left: '16px'}}>
                    <tr>
                        <th style={{width: '82px', borderBottom: 0}}>BR ID</th>
                        <th style={{width: '125px', borderBottom: 0}}>BR Name</th>
                        <th style={{width: '62px', borderBottom: 0}}>Active</th>
                        <th style={{width: '92px', borderBottom: 0}}>Work Date</th>
                        <th style={{width: '72px', borderBottom: 0}}>Contact</th>
                        <th style={{width: '72px', borderBottom: 0}}>PTR</th>
                        <th style={{width: '72px', borderBottom: 0}}>REF.</th>
                        <th style={{width: '72px', borderBottom: 0}}>G.A.</th>
                        <th style={{width: '100px', borderBottom: 0}}>Pack Sell</th>
                        <th style={{width: '72px', borderBottom: 0}}>CALL</th>
                        <th style={{width: '72px', borderBottom: 0}}>SUCCESS</th>
                        <th style={{width: '200px', borderRight: 0, borderBottom: 0}}></th>
                    </tr>
                    </thead>
                    <tbody className="MainTableBody">
                    {(function () {
                        return (
                            data.map(function (v) {
                                if (v.type == "details") {
                                    return $this.details(v);
                                } else {
                                    return $this.overview(v);
                                }
                            })
                        );
                    })()}
                    </tbody>
                </table>
            </div>
        );
    },
    details: function (v) {
        var $this = this;
        var style = {};
        if (!v.visible) {
            style.display = 'none';
        }

        return (
            <tr key={v.index} style={style} onClick={function () {$this.toggleViewDetails(v.index)}}>
                <td colSpan="12" style={{padding: 0}}>

                    <div className="panel panel-primary" style={{borderRadius: 0, margin: 0, border: 0}}>

                        <div className="panel-body" style={{margin: 0, padding: 0}}>

                            <div className="row">
                                <div className="col-md-12">
                                    <table className="table table-stripped DetailsTable alert-success"
                                           style={{marginBottom: '2px'}}>
                                        <thead>
                                        <tr>
                                            <td></td>
                                            <td>Contact</td>
                                            <td>S. Need</td>
                                            <td>Called</td>
                                            <td>Success</td>
                                            <td>PTR</td>
                                            <td>Called</td>
                                            <td>Success</td>
                                            <td>Refreshment</td>
                                            <td>Called</td>
                                            <td>Success</td>
                                            <td>G.A.</td>
                                            <td>Called</td>
                                            <td>Success</td>
                                            <td>P.S.</td>
                                            <td>Called</td>
                                            <td>Success</td>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <tr>
                                            <th>Total:</th>
                                            <th>{v.total.totalContact}</th>
                                            <th>{v.total.totalSuccessNeed}</th>
                                            <th>{v.total.totalCalled}</th>
                                            <th>{v.total.totalSuccess}</th>
                                            <th>{v.total.totalPTR}</th>
                                            <th>{v.total.totalPTRCalled}</th>
                                            <th>{v.total.totalPTRSuccess}</th>
                                            <th>{v.total.totalRef}</th>
                                            <th>{v.total.totalRefCalled}</th>
                                            <th>{v.total.totalRefSuccess}</th>
                                            <th>{v.total.totalGA}</th>
                                            <th>{v.total.totalGACalled}</th>
                                            <th>{v.total.totalGASuccess}</th>
                                            <th>{v.total.totalPackSell}</th>
                                            <th>{v.total.totalPackSellCalled}</th>
                                            <th>{v.total.totalPackSellSuccess}</th>
                                        </tr>
                                        <tr>
                                            <th>Daily:</th>
                                            <th>{v.daily.totalContact}</th>
                                            <th>{v.daily.totalSuccessNeed}</th>
                                            <th>{v.daily.totalCalled}</th>
                                            <th>{v.daily.totalSuccess}</th>
                                            <th>{v.daily.totalPTR}</th>
                                            <th>{v.daily.totalPTRCalled}</th>
                                            <th>{v.daily.totalPTRSuccess}</th>
                                            <th>{v.daily.totalRef}</th>
                                            <th>{v.daily.totalRefCalled}</th>
                                            <th>{v.daily.totalRefSuccess}</th>
                                            <th>{v.daily.totalGA}</th>
                                            <th>{v.daily.totalGACalled}</th>
                                            <th>{v.daily.totalGASuccess}</th>
                                            <th>{v.daily.totalPackSell}</th>
                                            <th>{v.daily.totalPackSellCalled}</th>
                                            <th>{v.daily.totalPackSellSuccess}</th>
                                        </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </td>
            </tr>
        );
    },
    overview: function (v) {
        var $this = this;
        var classes = [];
        if (!!v.selected) {
            classes.push("selected")
        }
        return (
            <tr key={v.index} onClick={function () {$this.toggleView(v.index)}}
                className={classes}>
                <td style={{width: '82px'}}>{v.br_id}</td>
                <td style={{width: '125px'}}>{v.br}</td>
                <td style={{width: '62px'}}>{!!v.active ? 'Yes' : 'No'}</td>
                <td style={{width: '92px'}}>{v.date}</td>

                <td style={{width: '72px'}}>{v.contacts}</td>
                <td style={{width: '72px'}}>{v.ptrs}</td>
                <td style={{width: '72px'}}>{v.refreshment_count}</td>
                <td style={{width: '72px'}}>{v.give_away_count}</td>
                <td style={{width: '100px'}}>{v.packsell_count}</td>

                <td style={{width: '72px'}}>{v.calls}</td>
                <td style={{width: '72px'}}>{v.success}</td>
                <td style={{width: '200px', borderRight: 0}}>
                    <a className="btn btn-sm btn-primary btn-block" href="#/work-day-details">Details</a>
                </td>
            </tr>
        );
    },
    toggleView: function (index) {
        var $this = this;
        var detailsIndex = index + 1;
        $this.state.data.forEach(function (v) {
            v.selected = (v.index == index)
            v.visible = ((v.index == detailsIndex) ? !v.visible : !!v.visible)
            return v;
        });

        if (!$this.state.data[detailsIndex].__isPresent) {
            $.ajax({
                url: '/br-activity-summary',
                data: {
                    'brId': $this.state.data[index].br_id,
                    'workDate': $this.state.data[index].date,
                    'workDate.__range': site.hash.params()["work-date-range"]
                },
                success: function (js) {
                    $this.state.data[detailsIndex].daily = js.daily;
                    $this.state.data[detailsIndex].total = js.total;
                    $this.state.data[detailsIndex].__isPresent = true;
                    $this.setState({__render: !$this.state.__render});
                }
            });
        }

        $this.setState({__render: !$this.state.__render});
    },
    toggleViewDetails: function (index) {
        var $this = this;
        $this.state.data.forEach(function (v) {
            v.visible = ((v.index == index) ? !v.visible : !!v.visible)
            return v;
        });

        $this.setState({__render: !$this.state.__render});
    },
    tableHeight: function (height) {
        return height - 60;
    },
    bodyHeight: function () {
        return $(window).height() - 52;
    },
    updateData: function (data) {
        var $this = this;
        this.setState(this.interceptState({data: data, __render: !$this.state.__render}));
    },
    interceptState: function (state) {
        state.data.forEach(function (v, i) {
            v.index = i;
        });
        return state;
    }
});