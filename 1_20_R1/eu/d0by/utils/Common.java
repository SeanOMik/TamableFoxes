package eu.d0by.utils;

import eu.d0by.utils.color.IridiumColorAPI;
import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class Common
{
	public static String colorize( String string )
	{
		return IridiumColorAPI.process( string );
	}
   
	public static ChatColor fromHex( String string )
	{
		return IridiumColorAPI.getColor( string.replace("#", "") );
	}
	
	public static ChatColor fromString( String string )
	{
		return IridiumColorAPI.getColorByString( string );
	}
	
	public static String rainbow( String string, int saturation )
	{
		return IridiumColorAPI.rainbow( string, saturation );
	}
	
	public static boolean isValidLegacyColor( String string )
	{
		return IridiumColorAPI.isValidLegacyColor( string );
	}
	
	public static String CreateGradient( String chars, @Nonnull String start, @Nonnull String end, boolean hex )
	{
		Color color1 = new Color( 16777215 );
		Color color2 = new Color( 16777215 );
		
		if( hex )
		{
			color1 = new Color( Integer.parseInt( start.replace("#", ""), 16 ) );
			color2 = new Color( Integer.parseInt( end.replace("#", ""), 16 ) );
		}
		else
		{
			color1 = IridiumColorAPI.getJavaColorByString( start );
			color2 = IridiumColorAPI.getJavaColorByString( end );
		}
		
		return IridiumColorAPI.color( chars, color1, color2 );
	}
	
	public static List<String> colorize( List<String> list )
	{
		return list.stream().map( Common::colorize ).collect( Collectors.toList() );
	}
}