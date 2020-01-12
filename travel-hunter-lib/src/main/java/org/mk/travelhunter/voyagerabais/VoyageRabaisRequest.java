package org.mk.travelhunter.voyagerabais;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * A class representing a requets to the website "voyagesarabais.com" using their REST api.
 * Each request contains: 
 * A single date for the request a "flexMin"
 * corresponding to how many days to include in the results before the date. Max
 * of 3 a "flexMax" corresponding to how many days to include in the results
 * after the date
 */
public class VoyageRabaisRequest {

	public static final String DEFAULT_URL = "https://voyagesarabais.com/recherche-sud/gridhebdo";

	private final Map<String, String> form = new HashMap<>();
	private final String url;
	private @Getter String hotelCode;
	private @Getter String date;
	private @Getter String flexLow;
	private @Getter String flexHigh;

	public VoyageRabaisRequest() {
		this(DEFAULT_URL);
	}

	public VoyageRabaisRequest(String url) {
		this.url = url;
		setDefaultFormValues();
	}

	// 2GW == riu republica
	public void setHotelCode(String hotelCode) {
		this.hotelCode = hotelCode;
		setFormValue("noHotel", hotelCode);
	}

	public void setDate(String date) {
		this.date = date;
		setFormValue("dateDep", date);
	}

	// Defaults to "2" even if less
	public void setFlexLow(String flexLow) {
		this.flexLow = flexLow;
		// How many days before the date to search (max 3)
		setFormValue("flexlow", flexLow);
	}

	public void setFlexHigh(String flexHigh) {
		this.flexHigh = flexHigh;
		// How many days in the after the date to search (max 3)
		setFormValue("flexhigh", flexHigh);
	}

	private void setFormValue(String key, String value) {
		form.put(key, value);
	}

	public String createBody() {

		StringBuilder sb = new StringBuilder();

		for (String key : form.keySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append("search%5B" + key + "%5D=" + form.get(key));
		}

		return sb.toString();

	}

	private void setDefaultFormValues() {

		form.put("gateway", "YUL");
		form.put("dateDep", "2018-09-23");
		form.put("duration", "7day,8day");
		form.put("destDep",
				"sP_57vo_57CW_2_h_I_bEqh_7_i_1d_12k_15_CU_9_o_2s_d_19_8_a_E_e_c_A_-_p_1s_r_1n_t_f_q_2Y_Xz_u_1kDi_Eh_1kGE_Y_3O_b_2aNK_2aPa_2aPy_g_16_m_2b5O_10_2b9G_l_Ku_3_4_6_L_B_R_Q_1c_y_._1j_sg_x_1e_1_bEql_6LZq_M_7fG5_X_3y_7gut_Dp_z_D_3J_T_2aF2_s_3YNi_3-fm_3-zD_1u_C_1v_bEqn_K_16k_V_1kA2_b1oA_3X_3N_2aVr_bEqr");
		form.put("noHotel", "rY");
		form.put("room1", "a:6:{i:0;s:2:\"40\";i:1;s:2:\"40\";i:2;s:0:\"\";i:3;s:0:\"\";i:4;s:0:\"\";i:5;s:0:\"\";}");
		form.put("room2", "");
		form.put("room3", "");
		form.put("room4", "");
		form.put("room5", "");
		form.put("room6", "");
		form.put("flex", "Y");
		form.put("flexhigh", "3");
		form.put("flexlow", "3");
		form.put("roomcallgroup", "[{\"_priority\":9,\"nb_rooms\":1,\"nb_adults\":2,\"non_adults\":[}");
		form.put("pricemax", "9000");
		form.put("pricemin", "1");
		form.put("allinclusive", "Y");
		form.put("beach", "");
		form.put("casino", "");
		form.put("family", "");
		form.put("golf", "");
		form.put("kitchenette", "");
		form.put("oceanview", "");
		form.put("miniclub", "");
		form.put("spa", "");
		form.put("wedding", "");
		form.put("adultonly", "");
		form.put("noextrasingle", "");
		form.put("villa", "");
		form.put("star", "1");
		form.put("starmax", "5");
		form.put("directflight", "Y");
		form.put("tourtodisplay", "CAH,VAT,VAC,VAX,SGN,SQV,SWG,WJV");
		form.put("uuid", "");
		form.put("nbRoomsMax", "");
		form.put("hotelDistanceMax", "");
	}

}
