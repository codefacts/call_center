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
            bodyHeight: this.bodyHeight(),
            data: this.props.data,
            __render: true
        });
    },
    componentDidMount: function () {
        var $this = this;
        $this.props.onInit($this);
        $(window).bind("resize", $this.onWindowResize);
    },
    componentWillUnmount: function () {
        var $this = this;
        $(window).unbind("resize", $this.onWindowResize);
    },
    onWindowResize: function () {
        var $this = this;
        $this.setState({bodyHeight: $this.bodyHeight(), __render: !$this.state.__render});
    },
    shouldComponentUpdate: function (nextProps, nextState) {
        return this.state.__render !== nextState.__render;
    },
    render: function () {
        console.log("render: TablePrimary");
        var $this = this;
        var data = $this.state.data;
        return (
            <div className="table-responsive TablePrimary"
                 style={{border: '1px solid #ddd', height: $this.state.bodyHeight + 'px'}}>

                <table className="table table-bordered table-hover table-condensed MainTable"
                       style={{border: 0, borderTop: '1px solid #ddd'}}>

                    <thead>
                    <tr>
                        <th className="bg-primary">Distributin House</th>
                        <th className="bg-primary">BR ID</th>
                        <th className="bg-primary">BR Name</th>
                        <th className="bg-success">Work Date</th>
                        <th className="bg-success">Contact</th>
                        <th className="bg-danger">PTR</th>
                        <th className="bg-warning">CALL</th>
                        <th className="bg-warning">SUCCESS</th>
                        <th className="bg-warning"></th>
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
            <tr key={$this.genDetailsKey(v)} style={style}
                onClick={function () {$this.toggleViewDetails(v.index)}}>
                <td colSpan="12" style={{padding: 0}}>

                    <site.reactjs.DERBY_JAN_2016.BrSummary data={v}/>

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
            <tr key={$this.genKey(v)}
                className={classes}>
                <td className="bg-primary"
                    onClick={function () {$this.toggleView(v.index)}}>{v.house}</td>
                <td className="bg-primary"
                    onClick={function () {$this.toggleView(v.index)}}>{v.br_id}</td>
                <td className="bg-primary"
                    onClick={function () {$this.toggleView(v.index)}}>{v.br}</td>
                <td className="bg-success"
                    onClick={function () {$this.toggleView(v.index)}}>{v.date}</td>

                <td className="bg-success"
                    onClick={function () {$this.toggleView(v.index)}}>{v.contacts}</td>
                <td className="bg-danger"
                    onClick={function () {$this.toggleView(v.index)}}>{v.ptrs}</td>

                <td className="bg-warning"
                    onClick={function () {$this.toggleView(v.index)}}>{v.calls}</td>
                <td className="bg-warning"
                    onClick={function () {$this.toggleView(v.index)}}>{v.success}</td>
                <td className="bg-warning">
                    <span className="btn btn-sm btn-primary btn-block" style={{width: '100px'}}
                          onClick={function () {$this.gotoStep2(v)}}>Details</span>
                </td>
            </tr>
        );
    },
    genDetailsKey: function (v) {
        return this.genKey(v) + ':details';
    },
    genKey: function (v) {
        return v.br_id + ':' + v.date;
    },
    gotoStep2: function (v) {
        var params = site.hash.params();
        params['brId'] = v.br_id;
        params["workDate"] = v.date;
        params['distributionHouseId'] = v.house_id;
        params['areaId'] = v.area_id;

        site.hash.goto('/work-day-details', params);
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
        return $(window).height() - 100;
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