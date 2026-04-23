// with downsampleddata as 
//  (	select lttb_with_parallel_arrays(
// 		array(select ts from metrics order by ts),
// 		array(select reading from metrics order by ts)
// 		,8) as lttb)
// select unnest(lttb['0'])::TIMESTAMP as ts,unnest(lttb['1']) as reading
// FROM downsampleddata;

function lttb_with_parallel_arrays(xarray,yarray,threshold) {		
        var data_length = xarray.length;
        if (threshold >= data_length || threshold === 0) {
            return Object.assign({}, [xarray,yarray]); // Nothing to do
        }

        var sampledx = [],
			sampledy = [],
            sampled_index = 0;

        // Bucket size. Leave room for start and end data points
        var every = (data_length - 2) / (threshold - 2);

        var a = 0,  // Initially a is the first point in the triangle
            max_area_point,
            max_area,
            area,
            next_a;

        sampledx[ sampled_index ] = xarray[a];
		sampledy[ sampled_index++ ] = yarray[a]; // Always add the first point

        for (var i = 0; i < threshold - 2; i++) {

            // Calculate point average for next bucket (containing c)
            var avg_x = 0,
                avg_y = 0,
                // look one bucket ahead to find where the next group of data begins
                avg_range_start  = Math.floor( ( i + 2 ) * every ) + 1,
                // look where the next bucket should end
                avg_range_end    = Math.floor( ( i + 2 ) * every ) + 1;
            // Temp/ ghost point is the average point in the next bucket
            avg_range_end = avg_range_end < data_length ? avg_range_end : data_length;

            var avg_range_length = avg_range_end - avg_range_start;
            // go through the next bucket to identify the average point
            for ( ; avg_range_start<avg_range_end; avg_range_start++ ) {
              avg_x += xarray[ avg_range_start ] * 1; // * 1 enforces Number (value may be Date)
              avg_y += yarray[ avg_range_start ] * 1;
            }
            avg_x /= avg_range_length;
            avg_y /= avg_range_length;

            // Get the range for this bucket
            var range_offs = Math.floor( (i + 0) * every ) + 1,
                range_to   = Math.floor( (i + 1) * every ) + 1;

            // Point a
            var point_a_x = xarray[ a ] * 1, // enforce Number (value may be Date)
                point_a_y = yarray[ a ] * 1;

            max_area = area = -1;

            for ( ; range_offs < range_to; range_offs++ ) {
                // Calculate triangle area over three buckets
                area = Math.abs( ( point_a_x - avg_x ) * ( yarray[ range_offs ] - point_a_y ) -
                            ( point_a_x - xarray[ range_offs ] ) * ( avg_y - point_a_y )
                          ) * 0.5;
                if ( area > max_area ) {
                    max_area = area;
                    max_area_point = [xarray[range_offs],yarray[range_offs]];
                    next_a = range_offs; // Next a is this b
                }
            }

            sampledx[ sampled_index ] = max_area_point[0];
			sampledy[ sampled_index++ ] = max_area_point[1]; // Pick this point from the bucket
            a = next_a; // This a is the next a (chosen b)
        }

        sampledx[ sampled_index ] = xarray[data_length - 1];
		sampledy[ sampled_index++ ] = yarray[data_length - 1]; // Always add last				
		
        return Object.assign({}, [sampledx,sampledy]);
    }
